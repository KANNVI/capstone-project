package com.bloodconnect.dto;

import com.bloodconnect.model.RequestStatus;
import com.bloodconnect.model.Urgency;

import java.time.LocalDateTime;

public record BloodRequestResponse(
        Long id, String requesterName, String patientName, String bloodGroup,
        int unitsRequired, int unitsFulfilled, String hospital, String city,
        Double latitude, Double longitude, Urgency urgency, RequestStatus status,
        String contactPhone, String notes, LocalDateTime createdAt,
        long donorsNotified, long donorsAccepted
) {}
