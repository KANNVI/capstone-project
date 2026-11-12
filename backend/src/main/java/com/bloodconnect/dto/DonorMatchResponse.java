package com.bloodconnect.dto;

import com.bloodconnect.model.DonorRequestStatus;

public record DonorMatchResponse(
        Long donorRequestId, Long donorId, String name, String bloodGroup,
        Double distanceKm, boolean exactMatch, DonorRequestStatus status, String phone
) {}
