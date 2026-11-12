package com.bloodconnect.dto;

/** Used by GET /api/donors/matching (ad-hoc search, no phone number exposed). */
public record DonorSearchResult(Long donorId, String name, String bloodGroup, double distanceKm, boolean exactMatch) {}
