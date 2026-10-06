package com.clawpet.controller;

import com.clawpet.common.Result;
import com.clawpet.entity.AdoptApplication;
import com.clawpet.entity.AdoptRecord;
import com.clawpet.entity.PetImage;
import com.clawpet.entity.PetInfo;
import com.clawpet.mapper.AdoptRecordMapper;
import com.clawpet.mapper.PetImageMapper;
import com.clawpet.mapper.PetInfoMapper;
import com.clawpet.service.AdoptService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 领养控制器 - 领养申请提交、审核、取消、记录查询
 */
@RestController
@RequestMapping("/api/adopt")
public class AdoptController {

    private final AdoptService adoptService;
    private final AdoptRecordMapper adoptRecordMapper;
    private final PetInfoMapper petInfoMapper;
    private final PetImageMapper petImageMapper;

    public AdoptController(AdoptService adoptService, AdoptRecordMapper adoptRecordMapper, PetInfoMapper petInfoMapper, PetImageMapper petImageMapper) {
        this.adoptService = adoptService;
        this.adoptRecordMapper = adoptRecordMapper;
        this.petInfoMapper = petInfoMapper;
        this.petImageMapper = petImageMapper;
    }

    /** 获取当前用户已领养的宠物列表 */
    @GetMapping("/my-pets")
    public Result<List<Map<String, Object>>> myPets(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<AdoptRecord> records = adoptRecordMapper.selectList(
                new LambdaQueryWrapper<AdoptRecord>().eq(AdoptRecord::getUserId, userId));
        List<Map<String, Object>> list = new ArrayList<>();
        for (AdoptRecord r : records) {
            PetInfo pet = petInfoMapper.selectById(r.getPetId());
            if (pet == null) continue;
            // 单独查 pet_image 表，因为 imageUrls 是 @TableField(exist = false)
            List<PetImage> imgs = petImageMapper.selectList(
                    new LambdaQueryWrapper<PetImage>()
                            .eq(PetImage::getPetId, pet.getId())
                            .orderByDesc(PetImage::getIsCover)
                            .orderByAsc(PetImage::getSort)
            );
            String cover = imgs.isEmpty() ? "" : imgs.get(0).getUrl();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("recordId", r.getId());
            item.put("petId", pet.getId());
            item.put("petName", pet.getName());
            item.put("breed", pet.getBreed());
            item.put("petCover", cover);
            item.put("adoptedAt", r.getCreatedAt());
            list.add(item);
        }
        return Result.success(list);
    }

    /** 提交领养申请 */
    @PostMapping("/apply")
    public Result<Void> apply(Authentication authentication, @RequestBody AdoptApplication app) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return Result.badRequest("管理员不能提交领养申请");
        app.setUserId(userId);
        adoptService.apply(app);
        return Result.success("申请成功", null);
    }

    /**
     * 分页查询领养申请列表
     * 管理员看到全部，普通用户只看到自己的
     */
    @GetMapping("/list")
    public Result<Page<AdoptApplication>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .findFirst().map(a -> a.getAuthority()).orElse("");
        if ("ROLE_ADMIN".equals(role)) {
            return Result.success(adoptService.listAll(page, size, status));
        } else {
            Long userId = (Long) authentication.getPrincipal();
            return Result.success(adoptService.listByUser(page, size, userId));
        }
    }

    /** 根据 ID 获取申请详情 */
    @GetMapping("/{id}")
    public Result<AdoptApplication> getById(@PathVariable Long id) {
        return Result.success(adoptService.getById(id));
    }

    /** 管理员审核领养申请 */
    @PutMapping("/review")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> review(Authentication authentication, @RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String status = (String) body.get("status");
        String rejectReason = (String) body.getOrDefault("rejectReason", null);
        Long reviewUserId = (Long) authentication.getPrincipal();
        adoptService.review(id, status, rejectReason, reviewUserId);
        return Result.success();
    }

    /** 用户取消自己的待审核申请 */
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        adoptService.cancel(id, userId);
        return Result.success();
    }

    /** 管理员删除申请记录 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        adoptService.delete(id);
        return Result.success("删除成功", null);
    }
}
