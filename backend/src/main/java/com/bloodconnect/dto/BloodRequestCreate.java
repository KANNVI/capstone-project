package com.bloodconnect.dto;

import com.bloodconnect.model.Urgency;
import jakarta.validation.constraints.*;

public record BloodRequestCreate(
        String patientName,
        @NotBlank String bloodGroup,
        @NotNull @Min(1) @Max(10) Integer unitsRequired,
        @NotBlank String hospital,
        String city,
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotNull Urgency urgency,
        @NotBlank String contactPhone,
        @Size(max = 500) String notes
) {}
