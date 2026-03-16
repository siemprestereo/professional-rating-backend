package com.example.waiter_rating.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = true)
    private AppUser client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = true)  // ← ahora nullable
    private AppUser professional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_history_id", nullable = true)
    private WorkHistory workHistory;

    // Snapshot del nombre del profesional al momento de la calificación
    @Column(name = "professional_name", length = 100)
    private String professionalName;

    // Snapshot del nombre del negocio al momento de la calificación
    @Column(name = "business_name", length = 100)
    private String businessName;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int score;

    @Size(max = 140)
    @Column(length = 140)
    private String comment;

    @Column(name = "service_date")
    private LocalDateTime serviceDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public boolean canEditOrDelete() {
        if (this.createdAt == null) return false;
        long minutesPassed = ChronoUnit.MINUTES.between(this.createdAt, LocalDateTime.now());
        return minutesPassed < 30;
    }
}