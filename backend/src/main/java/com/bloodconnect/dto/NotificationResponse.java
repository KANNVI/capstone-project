package com.bloodconnect.dto;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, String message, boolean seen, LocalDateTime createdAt) {}
