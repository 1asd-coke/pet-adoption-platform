package com.clawpet.service;

import com.clawpet.entity.PetFavorite;

import java.util.List;

public interface FavoriteService {
    List<PetFavorite> listByUser(Long userId);
    void add(Long userId, Long petId);
    void remove(Long userId, Long petId);
    boolean check(Long userId, Long petId);
    void deleteByUserId(Long userId);
}
