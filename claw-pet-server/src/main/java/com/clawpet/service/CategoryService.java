package com.clawpet.service;

import com.clawpet.entity.PetCategory;

import java.util.List;

public interface CategoryService {
    List<PetCategory> listAll();
    PetCategory getById(Long id);
    Long create(PetCategory category);
    void update(PetCategory category);
    void delete(Long id);
}
