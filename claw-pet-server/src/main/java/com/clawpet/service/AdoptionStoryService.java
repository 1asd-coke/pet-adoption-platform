package com.clawpet.service;

import com.clawpet.entity.AdoptionStory;

import java.util.List;

public interface AdoptionStoryService {
    /** 展示被管理员标记的故事（前台用） */
    List<AdoptionStory> listShowcase();

    /** 查询某用户发布的所有故事 */
    List<AdoptionStory> listByUser(Long userId);

    /** 管理员查全量 */
    List<AdoptionStory> listAll();

    /** 领养人发布 */
    void publish(Long userId, Long recordId, String story);

    /** 领养人更新 */
    void update(Long userId, Long recordId, String story);

    /** 管理员切换展示 */
    void toggleShowcase(Long id, boolean showcase);
}
