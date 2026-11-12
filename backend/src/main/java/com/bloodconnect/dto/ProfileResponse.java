package com.bloodconnect.dto;

import com.bloodconnect.model.Role;

import java.time.LocalDate;

/** User + (if donor) donor details, in one object for the frontend. */
public record ProfileResponse(
        Long id, String name, String email, String phone, Role role, String city, boolean verified,
        String bloodGroup, Boolean available, LocalDate lastDonationDate, LocalDate nextEligibleDate,
        boolean eligibleNow, Double latitude, Double longitude
) {}
