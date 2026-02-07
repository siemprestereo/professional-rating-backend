package com.example.waiter_rating.repository;


import com.example.waiter_rating.model.PasswordResetToken;
import com.example.waiter_rating.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUserAndUsedFalse(AppUser user);
    void deleteByUser(AppUser user);
}
