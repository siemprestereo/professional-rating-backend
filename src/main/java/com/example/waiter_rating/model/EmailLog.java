package com.example.waiter_rating.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String type; // INDIVIDUAL | BROADCAST

    @Column(nullable = false)
    private String subject;

    private String recipientEmail;
    private String recipientName;

    @Column(length = 20)
    private String targetRole; // ALL | PROFESSIONAL | CLIENT (broadcast only)

    @Column(nullable = false, length = 100)
    private String senderAlias;

    @Column(nullable = false, length = 300)
    private String bodyPreview;

    @Column(nullable = false)
    private int recipientsCount;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}
