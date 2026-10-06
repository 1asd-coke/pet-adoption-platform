package com.clawpet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 通知实体 - 对应 notification 表
 */
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 接收通知的用户 ID */
    private Long userId;
    /** 通知类型：COMMENT_REPLY / ADOPT_RESULT / NEW_APPLICATION */
    private String type;
    /** 通知标题 */
    private String title;
    /** 通知内容 */
    private String content;
    /** 关联业务 ID（评论ID / 申请ID） */
    private Long relatedId;
    /** 是否已读：0 未读 / 1 已读 */
    private Integer isRead;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // ====== 非数据库字段 ======

    /** 通知类型的中文标签 */
    @TableField(exist = false)
    private String typeLabel;

    /** COMMENT_REPLY 类型时，评论所属的宠物 ID */
    @TableField(exist = false)
    private Long petId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }

    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getTypeLabel() { return typeLabel; }
    public void setTypeLabel(String typeLabel) { this.typeLabel = typeLabel; }

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
}
