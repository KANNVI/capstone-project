package com.blooddonor.controller;

import com.blooddonor.model.AppUser;
import com.blooddonor.repo.UserRepository;
import com.blooddonor.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    record RegisterReq(@NotBlank String name, @Email @NotBlank String email, @Size(min = 6) String password) {}
    record LoginReq(@NotBlank String email, @NotBlank String password) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtUtil jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    @PostMapping("/register")
    public Map<String, String> register(@Valid @RequestBody RegisterReq r) {
        String email = r.email().toLowerCase();
        if (users.findByEmail(email).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        AppUser u = new AppUser();
        u.name = r.name(); u.email = email; u.password = encoder.encode(r.password()); u.role = "USER";
        users.save(u);
        return token(u);
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginReq r) {
        AppUser u = users.findByEmail(r.email().toLowerCase())
                .filter(x -> encoder.matches(r.password(), x.password))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return token(u);
    }

    private Map<String, String> token(AppUser u) {
        return Map.of("token", jwt.create(u.email, u.role), "name", u.name, "role", u.role);
    }
}
