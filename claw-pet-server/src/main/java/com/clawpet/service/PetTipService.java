package com.clawpet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawpet.entity.PetTip;

public interface PetTipService {
    Page<PetTip> listPublic(int page, int size);
    PetTip detail(Long id);
    void add(PetTip tip);
    void update(PetTip tip);
    void delete(Long id);
}
