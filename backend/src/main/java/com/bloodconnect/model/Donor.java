package com.bloodconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "donors")
@Getter @Setter @NoArgsConstructor
public class Donor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false, length = 3)
    private String bloodGroup;

    private Double latitude;
    private Double longitude;

    private boolean available = true;

    private LocalDate lastDonationDate;

    public boolean isEligible(int minGapDays) {
        return lastDonationDate == null
                || !lastDonationDate.plusDays(minGapDays).isAfter(LocalDate.now());
    }
}
