package com.example.waiter_rating.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "education")
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "professional_id", nullable = false)
    private AppUser professional;

    @Column(name = "institution", length = 200)
    private String institution;

    @Column(name = "degree", length = 200)
    private String degree;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "currently_studying")
    private Boolean currentlyStudying = false;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;


}