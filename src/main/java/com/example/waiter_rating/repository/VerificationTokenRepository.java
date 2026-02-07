package com.example.waiter_rating.repository;


import com.example.waiter_rating.model.AppUser;
import com.example.waiter_rating.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUserAndUsedFalse(AppUser user);
    void deleteByUser(AppUser user);
}
