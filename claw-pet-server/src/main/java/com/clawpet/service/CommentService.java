package com.clawpet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clawpet.entity.PetComment;

public interface CommentService {
    Page<PetComment> listByPet(int page, int size, Long petId);
    Page<PetComment> listByUser(int page, int size, Long userId);
    Page<PetComment> listRepliedToUser(int page, int size, Long userId);
    Long add(PetComment comment);
    void update(PetComment comment);
    void delete(Long id);
    void deleteByUserId(Long userId);
}
