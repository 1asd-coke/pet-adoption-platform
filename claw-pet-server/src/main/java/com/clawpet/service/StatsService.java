package com.clawpet.service;

import com.clawpet.dto.StatsDTO;

import java.util.Map;

public interface StatsService {
    StatsDTO getHomeStats();
    Map<String, Object> getDashboardStats();
}
