package com.bloodconnect.dto;

import java.util.Map;

public record AdminStats(
        long totalUsers, long donors, long requesters, long totalRequests,
        long pendingRequests, long activeRequests, long completedRequests, long totalDonations,
        Map<String, Long> donorsByBloodGroup
) {}
