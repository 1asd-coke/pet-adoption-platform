package com.clawpet.service;

import com.clawpet.entity.AdoptionGuide;

import java.util.List;

public interface AdoptionGuideService {
    List<AdoptionGuide> list();
    void update(AdoptionGuide guide);
}
