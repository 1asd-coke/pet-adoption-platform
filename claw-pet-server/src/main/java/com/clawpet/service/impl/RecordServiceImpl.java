package com.clawpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawpet.entity.AdoptRecord;
import com.clawpet.entity.FollowupRecord;
import com.clawpet.entity.PetInfo;
import com.clawpet.entity.User;
import com.clawpet.mapper.AdoptRecordMapper;
import com.clawpet.mapper.FollowupRecordMapper;
import com.clawpet.mapper.PetInfoMapper;
import com.clawpet.mapper.UserMapper;
import com.clawpet.service.RecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecordServiceImpl implements RecordService {

    private final AdoptRecordMapper adoptRecordMapper;
    private final FollowupRecordMapper followupRecordMapper;
    private final PetInfoMapper petInfoMapper;
    private final UserMapper userMapper;

    public RecordServiceImpl(AdoptRecordMapper adoptRecordMapper,
                             FollowupRecordMapper followupRecordMapper,
                             PetInfoMapper petInfoMapper,
                             UserMapper userMapper) {
        this.adoptRecordMapper = adoptRecordMapper;
        this.followupRecordMapper = followupRecordMapper;
        this.petInfoMapper = petInfoMapper;
        this.userMapper = userMapper;
    }

    private void enrichRecord(AdoptRecord record) {
        if (record == null) return;
        if (record.getPetId() != null) {
            PetInfo pet = petInfoMapper.selectById(record.getPetId());
            if (pet != null) record.setPetName(pet.getName());
        }
        if (record.getUserId() != null) {
            User user = userMapper.selectById(record.getUserId());
            if (user != null) {
                record.setAdopterName(user.getNickname() != null && !user.getNickname().isEmpty()
                        ? user.getNickname() : user.getUsername());
            }
        }
    }

    @Override
    public Page<AdoptRecord> listAll(int page, int size) {
        Page<AdoptRecord> p = new Page<>(page, size);
        Page<AdoptRecord> result = adoptRecordMapper.selectPage(p,
                new LambdaQueryWrapper<AdoptRecord>()
                        .orderByDesc(AdoptRecord::getCreatedAt));
        result.getRecords().forEach(this::enrichRecord);
        return result;
    }

    @Override
    public AdoptRecord getById(Long id) {
        AdoptRecord record = adoptRecordMapper.selectById(id);
        if (record == null) throw new RuntimeException("领养记录不存在");
        enrichRecord(record);
        return record;
    }

    @Override
    @Transactional
    public void addFollowup(FollowupRecord followup) {
        // 检查是否 30 天内已回访
        List<FollowupRecord> recent = followupRecordMapper.selectList(
                new LambdaQueryWrapper<FollowupRecord>()
                        .eq(FollowupRecord::getRecordId, followup.getRecordId())
                        .ge(FollowupRecord::getCreatedAt, LocalDateTime.now().minusDays(30))
                        .last("LIMIT 1")
        );
        if (!recent.isEmpty()) {
            throw new RuntimeException("该领养记录 30 天内已回访，请每月回访一次");
        }
        followupRecordMapper.insert(followup);
        AdoptRecord record = adoptRecordMapper.selectById(followup.getRecordId());
        if (record != null) {
            record.setFollowupStatus("done");
            adoptRecordMapper.updateById(record);
        }
    }

    @Override
    public List<FollowupRecord> getFollowups(Long recordId) {
        return followupRecordMapper.selectList(
                new LambdaQueryWrapper<FollowupRecord>()
                        .eq(FollowupRecord::getRecordId, recordId)
                        .orderByDesc(FollowupRecord::getCreatedAt));
    }
}
