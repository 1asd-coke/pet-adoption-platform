package com.clawpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawpet.dto.StatsDTO;
import com.clawpet.entity.AdoptApplication;
import com.clawpet.entity.PetCategory;
import com.clawpet.entity.PetInfo;
import com.clawpet.entity.User;
import com.clawpet.mapper.AdoptApplicationMapper;
import com.clawpet.mapper.PetCategoryMapper;
import com.clawpet.mapper.PetInfoMapper;
import com.clawpet.mapper.UserMapper;
import com.clawpet.service.StatsService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现 - 首页概览、管理员面板（分类/趋势/审核分布）
 */
@Service
public class StatsServiceImpl implements StatsService {

    private final PetInfoMapper petInfoMapper;
    private final UserMapper userMapper;
    private final PetCategoryMapper petCategoryMapper;
    private final AdoptApplicationMapper adoptApplicationMapper;

    public StatsServiceImpl(PetInfoMapper petInfoMapper,
                            UserMapper userMapper,
                            PetCategoryMapper petCategoryMapper,
                            AdoptApplicationMapper adoptApplicationMapper) {
        this.petInfoMapper = petInfoMapper;
        this.userMapper = userMapper;
        this.petCategoryMapper = petCategoryMapper;
        this.adoptApplicationMapper = adoptApplicationMapper;
    }

    /**
     * 获取首页统计数据：宠物/用户/领养申请各状态数量（缓存）
     */
    @Override
    @Cacheable(value = "statsHome")
    public StatsDTO getHomeStats() {
        StatsDTO stats = new StatsDTO();

        Long petCount = petInfoMapper.selectCount(
                new LambdaQueryWrapper<PetInfo>().eq(PetInfo::getDeleted, 0));
        Long availableCount = petInfoMapper.selectCount(
                new LambdaQueryWrapper<PetInfo>()
                        .eq(PetInfo::getStatus, "available")
                        .eq(PetInfo::getDeleted, 0));
        Long adoptingCount = petInfoMapper.selectCount(
                new LambdaQueryWrapper<PetInfo>()
                        .eq(PetInfo::getStatus, "adopting")
                        .eq(PetInfo::getDeleted, 0));
        Long adoptedCount = petInfoMapper.selectCount(
                new LambdaQueryWrapper<PetInfo>()
                        .eq(PetInfo::getStatus, "adopted")
                        .eq(PetInfo::getDeleted, 0));
        Long userCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getDeleted, 0));
        Long pendingCount = adoptApplicationMapper.selectCount(
                new LambdaQueryWrapper<AdoptApplication>()
                        .eq(AdoptApplication::getStatus, "pending"));

        stats.setPetCount(petCount);
        stats.setAvailableCount(availableCount);
        stats.setAdoptingCount(adoptingCount);
        stats.setAdoptedCount(adoptedCount);
        stats.setUserCount(userCount);
        stats.setPendingCount(pendingCount);

        return stats;
    }

    /**
     * 获取管理员面板统计数据：汇总、分类分布、趋势、审核状态分布、最近申请
     */
    @Override
    @Cacheable(value = "statsDashboard")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> dashboard = new HashMap<>();

        StatsDTO homeStats = getHomeStats();
        dashboard.put("summary", homeStats);

        // 分类分布
        List<PetCategory> categories = petCategoryMapper.selectList(null);
        List<Map<String, Object>> categoryDistribution = categories.stream().map(cat -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", cat.getName());
            Long count = petInfoMapper.selectCount(
                    new LambdaQueryWrapper<PetInfo>()
                            .eq(PetInfo::getCategoryId, cat.getId())
                            .eq(PetInfo::getDeleted, 0));
            item.put("count", count);
            return item;
        }).collect(Collectors.toList());
        dashboard.put("categoryDistribution", categoryDistribution);

        // 领养趋势（近 12 个月按月统计申请数 + 通过数）
        dashboard.put("trendData", getTrendData());

        // 审核状态分布
        Map<String, Object> statusDist = new LinkedHashMap<>();
        statusDist.put("pending", adoptApplicationMapper.selectCount(
                new LambdaQueryWrapper<AdoptApplication>().eq(AdoptApplication::getStatus, "pending")));
        statusDist.put("approved", adoptApplicationMapper.selectCount(
                new LambdaQueryWrapper<AdoptApplication>().eq(AdoptApplication::getStatus, "approved")));
        statusDist.put("rejected", adoptApplicationMapper.selectCount(
                new LambdaQueryWrapper<AdoptApplication>().eq(AdoptApplication::getStatus, "rejected")));
        dashboard.put("statusDistribution", statusDist);

        // 最近领养申请（按时间倒序取前10条）
        List<AdoptApplication> recentApps = adoptApplicationMapper.selectList(
                new LambdaQueryWrapper<AdoptApplication>()
                        .orderByDesc(AdoptApplication::getCreatedAt)
                        .last("LIMIT 10")
        );
        for (AdoptApplication app : recentApps) {
            enrichApplication(app);
        }
        dashboard.put("recentApplications", recentApps);

        return dashboard;
    }

    /** 获取近 12 个月的月度申请/通过统计 */
    private List<Map<String, Object>> getTrendData() {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 11; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            String label = monthStart.format(DateTimeFormatter.ofPattern("M月"));

            Long applyCount = adoptApplicationMapper.selectCount(
                    new LambdaQueryWrapper<AdoptApplication>()
                            .ge(AdoptApplication::getCreatedAt, monthStart.atStartOfDay())
                            .le(AdoptApplication::getCreatedAt, monthEnd.atTime(23, 59, 59)));

            Long approvedCount = adoptApplicationMapper.selectCount(
                    new LambdaQueryWrapper<AdoptApplication>()
                            .eq(AdoptApplication::getStatus, "approved")
                            .ge(AdoptApplication::getCreatedAt, monthStart.atStartOfDay())
                            .le(AdoptApplication::getCreatedAt, monthEnd.atTime(23, 59, 59)));

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", label);
            item.put("applyCount", applyCount);
            item.put("approvedCount", approvedCount);
            trend.add(item);
        }
        return trend;
    }

    /**
     * 填充领养申请的宠物名称和申请人名称
     */
    private void enrichApplication(AdoptApplication app) {
        if (app == null) return;

        if (app.getPetId() != null) {
            PetInfo pet = petInfoMapper.selectById(app.getPetId());
            if (pet != null) {
                app.setPetName(pet.getName());
            }
        }

        if (app.getUserId() != null) {
            User user = userMapper.selectById(app.getUserId());
            if (user != null) {
                app.setUserName(user.getNickname() != null && !user.getNickname().isEmpty()
                        ? user.getNickname() : user.getUsername());
            }
        }
    }
}
