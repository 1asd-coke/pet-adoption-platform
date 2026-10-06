package com.clawpet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawpet.entity.AdoptRecord;
import com.clawpet.entity.FollowupRecord;

import java.util.List;

public interface RecordService {
    Page<AdoptRecord> listAll(int page, int size);
    AdoptRecord getById(Long id);
    void addFollowup(FollowupRecord followup);
    List<FollowupRecord> getFollowups(Long recordId);
}
