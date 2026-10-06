package com.clawpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawpet.entity.AdoptionGuide;
import com.clawpet.mapper.AdoptionGuideMapper;
import com.clawpet.service.AdoptionGuideService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 领养须知服务实现
 */
@Service
public class AdoptionGuideServiceImpl implements AdoptionGuideService {

    private final AdoptionGuideMapper guideMapper;

    public AdoptionGuideServiceImpl(AdoptionGuideMapper guideMapper) {
        this.guideMapper = guideMapper;
    }

    @Override
    public List<AdoptionGuide> list() {
        return guideMapper.selectList(
                new LambdaQueryWrapper<AdoptionGuide>().orderByAsc(AdoptionGuide::getId));
    }

    @Override
    public void update(AdoptionGuide guide) {
        if (guideMapper.selectById(guide.getId()) == null) {
            guideMapper.insert(guide);
        } else {
            guideMapper.updateById(guide);
        }
    }
}
