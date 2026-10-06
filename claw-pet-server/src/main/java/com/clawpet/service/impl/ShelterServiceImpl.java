package com.clawpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawpet.entity.ShelterInfo;
import com.clawpet.mapper.ShelterInfoMapper;
import com.clawpet.service.ShelterService;
import org.springframework.stereotype.Service;

@Service
public class ShelterServiceImpl implements ShelterService {

    private final ShelterInfoMapper shelterMapper;

    public ShelterServiceImpl(ShelterInfoMapper shelterMapper) {
        this.shelterMapper = shelterMapper;
    }

    @Override
    public ShelterInfo get() {
        return shelterMapper.selectOne(null);
    }

    @Override
    public void update(ShelterInfo info) {
        ShelterInfo existing = shelterMapper.selectOne(null);
        if (existing != null) {
            info.setId(existing.getId());
            shelterMapper.updateById(info);
        } else {
            shelterMapper.insert(info);
        }
    }
}
