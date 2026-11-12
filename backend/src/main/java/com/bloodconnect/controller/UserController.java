package com.bloodconnect.controller;

import com.bloodconnect.dto.ProfileResponse;
import com.bloodconnect.security.CurrentUser;
import com.bloodconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final CurrentUser currentUser;
    private final UserService users;

    @GetMapping("/me")
    public ProfileResponse me() {
        return users.toProfile(currentUser.get());
    }
}
