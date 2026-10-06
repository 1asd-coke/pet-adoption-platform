package com.clawpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawpet.entity.PetTip;
import com.clawpet.mapper.PetTipMapper;
import com.clawpet.service.PetTipService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 小常识服务实现
 */
@Service
public class PetTipServiceImpl implements PetTipService {

    private final PetTipMapper tipMapper;

    public PetTipServiceImpl(PetTipMapper tipMapper) {
        this.tipMapper = tipMapper;
    }

    @Override
    public Page<PetTip> listPublic(int page, int size) {
        return tipMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<PetTip>()
                        .orderByDesc(PetTip::getSort)
                        .orderByDesc(PetTip::getPublishDate)
        );
    }

    @Override
    public PetTip detail(Long id) {
        return tipMapper.selectById(id);
    }

    @Override
    public void add(PetTip tip) {
        if (tip.getPublishDate() == null) {
            tip.setPublishDate(LocalDateTime.now());
        }
        if (tip.getSort() == null) {
            tip.setSort(0);
        }
        tipMapper.insert(tip);
    }

    @Override
    public void update(PetTip tip) {
        tipMapper.updateById(tip);
    }

    @Override
    public void delete(Long id) {
        tipMapper.deleteById(id);
    }
}
