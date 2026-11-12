package com.bloodconnect.dto;

import com.bloodconnect.model.DonorRequestStatus;

import java.time.LocalDateTime;

public record DonorRequestResponse(
        Long id, DonorRequestStatus status, Double distanceKm,
        LocalDateTime createdAt, LocalDateTime respondedAt, BloodRequestResponse request
) {}
