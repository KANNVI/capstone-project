package com.bloodconnect.controller;

import com.bloodconnect.dto.NotificationResponse;
import com.bloodconnect.security.CurrentUser;
import com.bloodconnect.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final CurrentUser currentUser;
    private final NotificationService service;

    @GetMapping
    public List<NotificationResponse> list() { return service.list(currentUser.get()); }

    @GetMapping("/unread-count")
    public Map<String, Long> unread() { return Map.of("count", service.unreadCount(currentUser.get())); }

    @PostMapping("/read-all")
    public Map<String, Boolean> readAll() {
        service.markAllRead(currentUser.get());
        return Map.of("ok", true);
    }
}
