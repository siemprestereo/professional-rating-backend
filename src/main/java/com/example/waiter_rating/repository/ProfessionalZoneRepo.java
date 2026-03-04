package com.example.waiter_rating.repository;

import com.example.waiter_rating.model.ProfessionalZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfessionalZoneRepo extends JpaRepository<ProfessionalZone, Long> {
    List<ProfessionalZone> findByCvId(Long cvId);
    void deleteByCvId(Long cvId);
}