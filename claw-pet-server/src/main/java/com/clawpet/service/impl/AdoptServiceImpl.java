package com.clawpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawpet.entity.AdoptApplication;
import com.clawpet.entity.AdoptRecord;
import com.clawpet.entity.PetInfo;
import com.clawpet.entity.User;
import com.clawpet.mapper.AdoptApplicationMapper;
import com.clawpet.mapper.AdoptRecordMapper;
import com.clawpet.mapper.PetInfoMapper;
import com.clawpet.mapper.UserMapper;
import com.clawpet.service.AdoptService;
import com.clawpet.service.NotificationService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 领养服务实现 - 申请提交、审核、取消，含通知推送
 */
@Service
public class AdoptServiceImpl implements AdoptService {

    private static String statusLabel(String status) {
        if (status == null) return "未知";
        return switch (status) {
            case "pending" -> "待审核";
            case "approved" -> "已通过";
            case "rejected" -> "已拒绝";
            default -> status;
        };
    }

    private final AdoptApplicationMapper applicationMapper;
    private final AdoptRecordMapper adoptRecordMapper;
    private final PetInfoMapper petInfoMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public AdoptServiceImpl(AdoptApplicationMapper applicationMapper,
                            AdoptRecordMapper adoptRecordMapper,
                            PetInfoMapper petInfoMapper,
                            UserMapper userMapper,
                            NotificationService notificationService) {
        this.applicationMapper = applicationMapper;
        this.adoptRecordMapper = adoptRecordMapper;
        this.petInfoMapper = petInfoMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    /**
     * 填充申请的宠物名称、品种和申请人的用户信息
     */
    private void enrichApplication(AdoptApplication app) {
        if (app == null) return;
        // 填充宠物名称
        if (app.getPetId() != null) {
            PetInfo pet = petInfoMapper.selectById(app.getPetId());
            if (pet != null) {
                app.setPetName(pet.getName());
                app.setPetBreed(pet.getBreed());
            }
        }
        // 填充申请人信息
        if (app.getUserId() != null) {
            User user = userMapper.selectById(app.getUserId());
            if (user != null) {
                app.setUserName(user.getNickname() != null && !user.getNickname().isEmpty()
                        ? user.getNickname() : user.getUsername());
                app.setUserAvatar(user.getAvatar());
                app.setPhone(user.getPhone());
                app.setRealName(user.getRealName());
            }
        }
        // 联系地址优先取申请表中的，没有则用用户 profile 中的地址
        if (!StringUtils.hasText(app.getAddress())) {
            app.setAddress(app.getContactAddress());
        }
        if (!StringUtils.hasText(app.getAddress()) && app.getUserId() != null) {
            User user = userMapper.selectById(app.getUserId());
            if (user != null && StringUtils.hasText(user.getAddress())) {
                app.setAddress(user.getAddress());
            }
        }
    }

    /**
     * 提交领养申请：检查宠物状态，更新为"领养中"，通知管理员
     */
    @Override
    @Transactional
    public void apply(AdoptApplication app) {
        // 检查宠物状态
        PetInfo pet = petInfoMapper.selectById(app.getPetId());
        if (pet == null) throw new RuntimeException("宠物不存在");
        if ("adopted".equals(pet.getStatus())) throw new RuntimeException("该宠物已被领养");
        if ("adopting".equals(pet.getStatus())) throw new RuntimeException("该宠物正在办理领养中");

        app.setStatus("pending");
        applicationMapper.insert(app);

        // 宠物改为"领养中"
        pet.setStatus("adopting");
        petInfoMapper.updateById(pet);

        // 通知所有管理员有新申请
        String petName = pet.getName() != null ? pet.getName() : "未知";
        List<User> admins = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getRole, "admin")
        );
        for (User admin : admins) {
            notificationService.create(admin.getId(), "NEW_APPLICATION",
                    "新领养申请",
                    "用户提交了 " + petName + " 的领养申请",
                    app.getId());
        }
    }

    /**
     * 分页查询某用户的申请列表
     */
    @Override
    public Page<AdoptApplication> listByUser(int page, int size, Long userId) {
        Page<AdoptApplication> p = new Page<>(page, size);
        Page<AdoptApplication> result = applicationMapper.selectPage(p,
                new LambdaQueryWrapper<AdoptApplication>()
                        .eq(AdoptApplication::getUserId, userId)
                        .orderByDesc(AdoptApplication::getCreatedAt));
        result.getRecords().forEach(this::enrichApplication);
        return result;
    }

    /**
     * 分页查询所有申请列表（管理员），可按状态筛选
     */
    @Override
    public Page<AdoptApplication> listAll(int page, int size, String status) {
        Page<AdoptApplication> p = new Page<>(page, size);
        Page<AdoptApplication> result = applicationMapper.selectPage(p,
                new LambdaQueryWrapper<AdoptApplication>()
                        .eq(StringUtils.hasText(status), AdoptApplication::getStatus, status)
                        .orderByDesc(AdoptApplication::getCreatedAt));
        result.getRecords().forEach(this::enrichApplication);
        return result;
    }

    /**
     * 根据 ID 获取申请详情
     */
    @Override
    public AdoptApplication getById(Long id) {
        AdoptApplication app = applicationMapper.selectById(id);
        if (app == null) {
            throw new RuntimeException("申请不存在");
        }
        enrichApplication(app);
        return app;
    }

    /**
     * 审核申请：通过则创建领养记录并更新宠物状态为"已领养"，
     * 拒绝则恢复宠物为"可领养"，发送通知给申请人
     */
    @Override
    @Transactional
    @CacheEvict(value = {"statsHome", "statsDashboard"}, allEntries = true)
    public synchronized void review(Long id, String status, String rejectReason, Long reviewUserId) {
        AdoptApplication app = applicationMapper.selectById(id);
        if (app == null) {
            throw new RuntimeException("申请不存在");
        }
        // 防止重复审核
        if (!"pending".equals(app.getStatus())) {
            throw new RuntimeException("该申请已审核（当前状态：" + statusLabel(app.getStatus()) + "），不可重复操作");
        }

        app.setStatus(status);
        app.setReviewUserId(reviewUserId);
        app.setReviewTime(LocalDateTime.now());

        if ("rejected".equals(status)) {
            app.setRejectReason(rejectReason);
        }

        applicationMapper.updateById(app);

        // 先查宠物信息（后面可能用到）
        PetInfo pet = petInfoMapper.selectById(app.getPetId());

        if ("approved".equals(status)) {
            AdoptRecord record = new AdoptRecord();
            record.setApplicationId(app.getId());
            record.setPetId(app.getPetId());
            record.setUserId(app.getUserId());
            record.setAdoptTime(LocalDateTime.now());
            record.setFollowupStatus("pending");
            adoptRecordMapper.insert(record);

            if (pet != null) {
                pet.setStatus("adopted");
                petInfoMapper.updateById(pet);
            }
        } else if ("rejected".equals(status)) {
            // 拒绝时宠物恢复为"可领养"
            if (pet != null) {
                pet.setStatus("available");
                petInfoMapper.updateById(pet);
            }
        }

        // 通知申请人审核结果
        String petName = pet != null ? pet.getName() : "未知";
        String resultLabel = "approved".equals(status) ? "已通过" : "未通过";
        notificationService.create(app.getUserId(), "ADOPT_RESULT",
                "领养申请" + resultLabel,
                "宠物: " + petName + "，申请状态: " + resultLabel,
                app.getId());
    }

    /**
     * 删除申请记录
     */
    @Override
    @Transactional
    public void delete(Long id) {
        applicationMapper.deleteById(id);
    }

    /**
     * 用户取消自己的待审核申请，恢复宠物状态为"可领养"
     */
    @Override
    @Transactional
    public void cancel(Long id, Long userId) {
        AdoptApplication app = applicationMapper.selectById(id);
        if (app == null) {
            throw new RuntimeException("申请不存在");
        }
        if (!app.getUserId().equals(userId)) {
            throw new RuntimeException("只能取消自己的申请");
        }
        if (!"pending".equals(app.getStatus())) {
            throw new RuntimeException("只能取消待审核的申请");
        }
        app.setStatus("rejected");
        app.setRejectReason("用户自行取消");
        applicationMapper.updateById(app);

        // 取消后宠物恢复为"可领养"
        PetInfo pet = petInfoMapper.selectById(app.getPetId());
        if (pet != null && "adopting".equals(pet.getStatus())) {
            pet.setStatus("available");
            petInfoMapper.updateById(pet);
        }
    }

    /**
     * 删除某用户的所有领养申请（级联删除用户时使用）
     */
    @Override
    public void deleteByUserId(Long userId) {
        applicationMapper.delete(new LambdaQueryWrapper<AdoptApplication>()
                .eq(AdoptApplication::getUserId, userId));
    }
}
