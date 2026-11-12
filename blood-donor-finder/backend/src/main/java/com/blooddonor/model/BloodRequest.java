package com.blooddonor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class BloodRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @NotBlank public String patient;
    @NotBlank public String bloodGroup;
    @Min(1) public int units = 1;
    @NotBlank public String hospital;
    @NotBlank public String city;
    public String urgency = "Urgent";      // Critical / Urgent / Planned
    public String requiredBy;
    public String status = "OPEN";         // OPEN / FULFILLED / CLOSED
    public boolean verified = false;
    @JsonIgnore public String ownerEmail;
    @ElementCollection(fetch = FetchType.EAGER) public Set<Long> alerted = new HashSet<>();
    @ElementCollection(fetch = FetchType.EAGER) public Set<Long> accepted = new HashSet<>();
}
