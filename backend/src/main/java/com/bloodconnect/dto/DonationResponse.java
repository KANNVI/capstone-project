package com.bloodconnect.dto;

import java.time.LocalDate;

public record DonationResponse(Long id, LocalDate donationDate, int units, String hospital, String patientBloodGroup) {}
