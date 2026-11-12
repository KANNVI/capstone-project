package com.bloodconnect.controller;

import com.bloodconnect.dto.AdminStats;
import com.bloodconnect.dto.AdminUserResponse;
import com.bloodconnect.dto.BloodRequestResponse;
import com.bloodconnect.security.CurrentUser;
import com.bloodconnect.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** All /api/admin/** routes are restricted to ROLE_ADMIN in SecurityConfig. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final CurrentUser currentUser;
    private final AdminService admin;

    @GetMapping("/stats")
    public AdminStats stats() { return admin.stats(); }

    @GetMapping("/users")
    public List<AdminUserResponse> users() { return admin.users(); }

    @PutMapping("/users/{id}/verify")
    public Map<String, Boolean> verify(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean value) {
        admin.setVerified(id, value);
        return Map.of("ok", true);
    }

    @PutMapping("/users/{id}/active")
    public Map<String, Boolean> active(@PathVariable Long id, @RequestParam boolean value) {
        admin.setActive(id, value, currentUser.get());
        return Map.of("ok", true);
    }

    @GetMapping("/requests")
    public List<BloodRequestResponse> requests() { return admin.requests(); }

    @DeleteMapping("/requests/{id}")
    public Map<String, String> removeRequest(@PathVariable Long id) {
        return Map.of("message", admin.removeRequest(id));
    }
}
