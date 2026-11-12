package com.bloodconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_requests")
@Getter @Setter @NoArgsConstructor
public class BloodRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requester_id")
    private User requester;

    private String patientName;

    @Column(nullable = false, length = 3)
    private String bloodGroup;

    @Column(nullable = false)
    private int unitsRequired;

    private int unitsFulfilled = 0;

    @Column(nullable = false)
    private String hospital;

    private String city;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Urgency urgency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(nullable = false, length = 20)
    private String contactPhone;

    @Column(length = 500)
    private String notes;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}
