package com.bloodconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "donation_history")
@Getter @Setter @NoArgsConstructor
public class DonationHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "donor_id")
    private Donor donor;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private BloodRequest bloodRequest;

    @Column(nullable = false)
    private LocalDate donationDate;

    private int units = 1;
}
