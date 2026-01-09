package com.example.waiter_rating.repository;


import com.example.waiter_rating.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessRepo extends JpaRepository<Business, Long> {

    Optional<Business> findByNameIgnoreCase(String name);
}
