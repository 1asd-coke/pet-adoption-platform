package com.clawpet.dto;

import lombok.Data;

/**
 * 首页统计 DTO - 汇总宠物、用户、领养申请数据
 */
@Data
public class StatsDTO {
    /** 宠物总数 */
    private Long petCount;
    /** 可领养数量 */
    private Long availableCount;
    /** 领养中数量 */
    private Long adoptingCount;
    /** 已领养数量 */
    private Long adoptedCount;
    /** 用户总数 */
    private Long userCount;
    /** 待处理申请数 */
    private Long pendingCount;
}
