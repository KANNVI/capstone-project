package com.bloodconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** One row = "this blood request was sent to this donor". */
@Entity
@Table(name = "donor_requests",
       uniqueConstraints = @UniqueConstraint(columnNames = {"blood_request_id", "donor_id"}))
@Getter @Setter @NoArgsConstructor
public class DonorRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "blood_request_id")
    private BloodRequest bloodRequest;

    @ManyToOne(optional = false)
    @JoinColumn(name = "donor_id")
    private Donor donor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DonorRequestStatus status = DonorRequestStatus.SENT;

    private Double distanceKm;

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}
