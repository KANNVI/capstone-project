package com.bloodconnect.dto;

import com.bloodconnect.model.Role;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "must be at least 6 characters") String password,
        @NotBlank @Pattern(regexp = "^[0-9+\\- ]{7,15}$", message = "enter a valid phone number") String phone,
        @NotNull Role role,
        String bloodGroup,
        String city,
        Double latitude,
        Double longitude,
        LocalDate lastDonationDate
) {}
