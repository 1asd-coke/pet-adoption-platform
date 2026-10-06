package com.clawpet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 宠物信息实体 - 对应 pet_info 表
 */
@Data
@TableName("pet_info")
public class PetInfo {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 宠物名称 */
    private String name;

    /** 分类 ID */
    private Long categoryId;

    /** 品种 */
    private String breed;

    /** 年龄描述 */
    private String age;

    /** 性别 */
    private String gender;

    /** 体重描述 */
    private String weight;

    /** 健康状况 */
    private String healthStatus;

    /** 疫苗接种情况 */
    private String vaccineStatus;

    /** 详细描述 */
    private String description;

    /** 领养状态：available / adopting / adopted */
    private String status;

    /** 发布者用户 ID */
    private Long publishUserId;

    /** 浏览次数 */
    private Integer viewCount;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;

    /** 非数据库字段：宠物图片 URL 列表 */
    @TableField(exist = false)
    private List<String> imageUrls;

    /** 非数据库字段：分类名称 */
    @TableField(exist = false)
    private String categoryName;
}
