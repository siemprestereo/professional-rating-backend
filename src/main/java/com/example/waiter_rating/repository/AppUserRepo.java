package com.example.waiter_rating.repository;


import com.example.waiter_rating.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.waiter_rating.model.enums.AuthProvider;
import java.util.Optional;

public interface AppUserRepo extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByEmailAndAuthProvider(String email, AuthProvider authProvider);

}

