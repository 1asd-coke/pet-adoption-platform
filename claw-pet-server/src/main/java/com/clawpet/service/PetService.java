package com.clawpet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawpet.entity.PetInfo;

public interface PetService {
    Page<PetInfo> list(int page, int size, Long categoryId, String status, String keyword,
                       String gender, String healthStatus);
    PetInfo getById(Long id);
    Long create(PetInfo pet);
    void update(PetInfo pet);
    void delete(Long id);
    void updateStatus(Long id, String status);
}
