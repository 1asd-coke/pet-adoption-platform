package com.clawpet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawpet.entity.AdoptApplication;

public interface AdoptService {
    void apply(AdoptApplication app);
    Page<AdoptApplication> listByUser(int page, int size, Long userId);
    Page<AdoptApplication> listAll(int page, int size, String status);
    AdoptApplication getById(Long id);
    void review(Long id, String status, String rejectReason, Long reviewUserId);
    void cancel(Long id, Long userId);
    void delete(Long id);
    void deleteByUserId(Long userId);
}
