package com.bloodconnect.dto;

import java.time.LocalDate;

/** Every field is optional - only non-null fields are applied. */
public record DonorProfileUpdate(
        Boolean available, LocalDate lastDonationDate, Double latitude, Double longitude, String city, String phone
) {}
