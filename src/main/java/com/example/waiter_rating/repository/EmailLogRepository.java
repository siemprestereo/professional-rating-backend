package com.example.waiter_rating.repository;

import com.example.waiter_rating.model.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    List<EmailLog> findTop100ByOrderBySentAtDesc();
}
