package com.blooddonor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
public class Donor {
    public static final int MIN_DAYS_BETWEEN_DONATIONS = 90;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @NotBlank public String name;
    @NotBlank public String bloodGroup;
    @Min(1) public int age;
    @Min(1) public double weight;
    @Pattern(regexp = "\\d{10}", message = "Phone must be 10 digits") public String phone;
    @NotBlank public String city;
    public LocalDate lastDonation;
    public boolean available = true;
    public boolean verified = false;
    @JsonIgnore public String ownerEmail;

    /** Eligibility: verified, available, age 18-65, weight >= 45 kg, 90 days since last donation. */
    public boolean isEligible() {
        boolean restedEnough = lastDonation == null
                || ChronoUnit.DAYS.between(lastDonation, LocalDate.now()) >= MIN_DAYS_BETWEEN_DONATIONS;
        return verified && available && age >= 18 && age <= 65 && weight >= 45 && restedEnough;
    }
}
