package com.clawpet.service;

import com.clawpet.entity.AdoptionGuide;

import java.util.List;

public interface AdoptionGuideService {
    List<AdoptionGuide> list();

    /** 新增或更新（id 为空或不存在时新增） */
    void update(AdoptionGuide guide);

    /** 删除章节 */
    void delete(Long id);
}
