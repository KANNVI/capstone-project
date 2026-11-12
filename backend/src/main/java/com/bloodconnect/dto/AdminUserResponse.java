package com.bloodconnect.dto;

import com.bloodconnect.model.Role;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id, String name, String email, String phone, Role role, String city,
        boolean verified, boolean active, String bloodGroup, LocalDateTime createdAt
) {}
