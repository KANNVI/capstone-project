package com.bloodconnect.service;

import com.bloodconnect.dto.AuthResponse;
import com.bloodconnect.dto.LoginRequest;
import com.bloodconnect.dto.RegisterRequest;
import com.bloodconnect.exception.ApiException;
import com.bloodconnect.model.Donor;
import com.bloodconnect.model.Role;
import com.bloodconnect.model.User;
import com.bloodconnect.repository.DonorRepository;
import com.bloodconnect.repository.UserRepository;
import com.bloodconnect.security.JwtUtil;
import com.bloodconnect.util.BloodCompatibility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final DonorRepository donors;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    private final UserService userService;

    @Transactional
    public AuthResponse register(RegisterRequest r) {
        String email = r.email().trim().toLowerCase();
        if (r.role() == Role.ADMIN) throw ApiException.forbidden("Admin accounts cannot be self-registered");
        if (users.existsByEmail(email)) throw ApiException.conflict("This email is already registered");

        if (r.role() == Role.DONOR) {
            if (!BloodCompatibility.isValid(r.bloodGroup()))
                throw ApiException.badRequest("Select a valid blood group");
            if (r.latitude() == null || r.longitude() == null)
                throw ApiException.badRequest("Donors must share their location");
            if (r.lastDonationDate() != null && r.lastDonationDate().isAfter(LocalDate.now()))
                throw ApiException.badRequest("Last donation date cannot be in the future");
        }

        User u = new User();
        u.setName(r.name().trim());
        u.setEmail(email);
        u.setPassword(encoder.encode(r.password()));
        u.setPhone(r.phone().trim());
        u.setRole(r.role());
        u.setCity(r.city() == null ? null : r.city().trim());
        users.save(u);

        if (r.role() == Role.DONOR) {
            Donor d = new Donor();
            d.setUser(u);
            d.setBloodGroup(r.bloodGroup());
            d.setLatitude(r.latitude());
            d.setLongitude(r.longitude());
            d.setLastDonationDate(r.lastDonationDate());
            d.setAvailable(true);
            donors.save(d);
        }
        return new AuthResponse(jwt.generate(u), userService.toProfile(u));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest r) {
        User u = users.findByEmail(r.email().trim().toLowerCase())
                .filter(x -> encoder.matches(r.password(), x.getPassword()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Wrong email or password"));
        if (!u.isActive()) throw ApiException.forbidden("This account has been blocked by an admin");
        return new AuthResponse(jwt.generate(u), userService.toProfile(u));
    }
}
