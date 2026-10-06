package com.clawpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawpet.entity.*;
import com.clawpet.mapper.*;
import com.clawpet.service.AdoptionStoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdoptionStoryServiceImpl implements AdoptionStoryService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AdoptionStoryMapper storyMapper;
    private final AdoptRecordMapper adoptRecordMapper;
    private final FollowupRecordMapper followupRecordMapper;
    private final PetInfoMapper petInfoMapper;
    private final UserMapper userMapper;
    private final PetImageMapper petImageMapper;

    public AdoptionStoryServiceImpl(AdoptionStoryMapper storyMapper,
                                    AdoptRecordMapper adoptRecordMapper,
                                    FollowupRecordMapper followupRecordMapper,
                                    PetInfoMapper petInfoMapper,
                                    UserMapper userMapper,
                                    PetImageMapper petImageMapper) {
        this.storyMapper = storyMapper;
        this.adoptRecordMapper = adoptRecordMapper;
        this.followupRecordMapper = followupRecordMapper;
        this.petInfoMapper = petInfoMapper;
        this.userMapper = userMapper;
        this.petImageMapper = petImageMapper;
    }

    @Override
    public List<AdoptionStory> listShowcase() {
        List<AdoptionStory> stories = storyMapper.selectList(
                new LambdaQueryWrapper<AdoptionStory>()
                        .eq(AdoptionStory::getShowcase, 1)
                        .orderByDesc(AdoptionStory::getUpdatedAt)
        );
        enrichEach(stories);
        return stories;
    }

    @Override
    public List<AdoptionStory> listAll() {
        List<AdoptionStory> stories = storyMapper.selectList(
                new LambdaQueryWrapper<AdoptionStory>()
                        .orderByDesc(AdoptionStory::getUpdatedAt)
        );
        enrichEach(stories);
        return stories;
    }

    @Override
    public List<AdoptionStory> listByUser(Long userId) {
        List<AdoptionStory> stories = storyMapper.selectList(
                new LambdaQueryWrapper<AdoptionStory>()
                        .eq(AdoptionStory::getUserId, userId)
                        .orderByDesc(AdoptionStory::getUpdatedAt)
        );
        enrichEach(stories);
        return stories;
    }

    @Override
    @Transactional
    public void publish(Long userId, Long recordId, String storyText) {
        AdoptRecord record = adoptRecordMapper.selectById(recordId);
        if (record == null) throw new RuntimeException("领养记录不存在");
        if (!userId.equals(record.getUserId())) throw new RuntimeException("无权操作");
        if (!StringUtils.hasText(storyText)) throw new IllegalArgumentException("故事内容不能为空");

        // 不限制重复发布，同一只宠物可发布多篇故事

        AdoptionStory story = new AdoptionStory();
        story.setRecordId(recordId);
        story.setPetId(record.getPetId());
        story.setUserId(userId);
        story.setStory(storyText);
        story.setShowcase(0);
        storyMapper.insert(story);
    }

    @Override
    @Transactional
    public void update(Long userId, Long recordId, String storyText) {
        AdoptionStory story = storyMapper.selectOne(
                new LambdaQueryWrapper<AdoptionStory>().eq(AdoptionStory::getRecordId, recordId));
        if (story == null) throw new RuntimeException("还没有发布过故事");
        if (!userId.equals(story.getUserId())) throw new RuntimeException("无权编辑");
        story.setStory(storyText);
        storyMapper.updateById(story);
    }

    @Override
    public void toggleShowcase(Long id, boolean showcase) {
        AdoptionStory story = storyMapper.selectById(id);
        if (story == null) throw new RuntimeException("故事不存在");
        story.setShowcase(showcase ? 1 : 0);
        storyMapper.updateById(story);
    }

    /** 批量填充关联数据 */
    private void enrichEach(List<AdoptionStory> list) {
        if (list.isEmpty()) return;

        // 收集 recordId 去重
        Set<Long> recordIds = list.stream().map(AdoptionStory::getRecordId).collect(Collectors.toSet());

        // 批量查回访图片（每个 recordId 的最新一条回访）
        Map<Long, FollowupRecord> followupMap = new HashMap<>();
        for (Long rid : recordIds) {
            FollowupRecord fu = followupRecordMapper.selectOne(
                    new LambdaQueryWrapper<FollowupRecord>()
                            .eq(FollowupRecord::getRecordId, rid)
                            .orderByDesc(FollowupRecord::getCreatedAt)
                            .last("LIMIT 1")
            );
            if (fu != null) followupMap.put(rid, fu);
        }

        // 批量查宠物
        Set<Long> petIds = list.stream().map(AdoptionStory::getPetId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, PetInfo> petMap = new HashMap<>();
        for (Long pid : petIds) {
            PetInfo p = petInfoMapper.selectById(pid);
            if (p != null) petMap.put(pid, p);
        }

        // 批量查用户
        Set<Long> userIds = list.stream().map(AdoptionStory::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> userMap = new HashMap<>();
        for (Long uid : userIds) {
            User u = userMapper.selectById(uid);
            if (u != null) userMap.put(uid, u);
        }

        for (AdoptionStory s : list) {
            FollowupRecord fu = followupMap.get(s.getRecordId());
            if (fu != null) {
                s.setImages(parseImages(fu.getImages()));
                if (fu.getFollowupTime() != null) {
                    s.setFollowupTime(fu.getFollowupTime().format(DATE_FORMAT));
                }
            }
            // 没回访时，回退到宠物封面图
            if (s.getImages() == null || s.getImages().isEmpty()) {
                List<PetImage> petImgs = petImageMapper.selectList(
                        new LambdaQueryWrapper<PetImage>()
                                .eq(PetImage::getPetId, s.getPetId())
                                .orderByDesc(PetImage::getIsCover)
                                .orderByAsc(PetImage::getSort)
                );
                if (!petImgs.isEmpty()) {
                    s.setImages(java.util.Collections.singletonList(petImgs.get(0).getUrl()));
                }
            }
            PetInfo pet = petMap.get(s.getPetId());
            if (pet != null) {
                s.setPetName(pet.getName());
                if (pet.getImageUrls() != null && !pet.getImageUrls().isEmpty()) {
                    s.setPetCover(pet.getImageUrls().get(0));
                }
            }
            User user = userMap.get(s.getUserId());
            if (user != null) {
                s.setAdopterName(user.getNickname() != null && !user.getNickname().isEmpty()
                        ? user.getNickname() : user.getUsername());
            }
        }
    }

    private List<String> parseImages(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        String trimmed = json.trim();
        if (!trimmed.startsWith("[")) return Collections.emptyList();
        trimmed = trimmed.replaceAll("[\\[\\]\"]", "");
        if (trimmed.isEmpty()) return Collections.emptyList();
        return Arrays.stream(trimmed.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
