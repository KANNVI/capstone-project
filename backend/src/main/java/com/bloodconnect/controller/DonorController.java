package com.bloodconnect.controller;

import com.bloodconnect.dto.*;
import com.bloodconnect.security.CurrentUser;
import com.bloodconnect.service.DonorService;
import com.bloodconnect.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donors")
@RequiredArgsConstructor
public class DonorController {
    private final CurrentUser currentUser;
    private final UserService users;
    private final DonorService donors;

    @PutMapping("/availability")
    @PreAuthorize("hasRole('DONOR')")
    public ProfileResponse availability(@Valid @RequestBody AvailabilityRequest r) {
        return users.setAvailability(currentUser.get(), r.available());
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('DONOR')")
    public ProfileResponse updateProfile(@RequestBody DonorProfileUpdate r) {
        return users.updateDonorProfile(currentUser.get(), r);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('DONOR')")
    public List<DonationResponse> history() {
        return donors.history(currentUser.get());
    }

    /** Ad-hoc matching: GET /api/donors/matching?bloodGroup=B+&lat=30.74&lng=76.64&radiusKm=25 */
    @GetMapping("/matching")
    @PreAuthorize("hasAnyRole('REQUESTER','ADMIN')")
    public List<DonorSearchResult> matching(@RequestParam String bloodGroup,
                                            @RequestParam double lat,
                                            @RequestParam double lng,
                                            @RequestParam(required = false) Double radiusKm) {
        return donors.search(bloodGroup, lat, lng, radiusKm, currentUser.get());
    }
}
