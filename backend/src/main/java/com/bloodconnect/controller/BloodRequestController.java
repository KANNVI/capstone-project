package com.bloodconnect.controller;

import com.bloodconnect.dto.BloodRequestCreate;
import com.bloodconnect.dto.BloodRequestResponse;
import com.bloodconnect.dto.DonorMatchResponse;
import com.bloodconnect.security.CurrentUser;
import com.bloodconnect.service.BloodRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class BloodRequestController {
    private final CurrentUser currentUser;
    private final BloodRequestService service;

    @PostMapping
    @PreAuthorize("hasRole('REQUESTER')")
    public BloodRequestResponse create(@Valid @RequestBody BloodRequestCreate body) {
        return service.create(body, currentUser.get());
    }

    @GetMapping
    @PreAuthorize("hasRole('REQUESTER')")
    public List<BloodRequestResponse> mine() {
        return service.listMine(currentUser.get());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('REQUESTER','ADMIN')")
    public BloodRequestResponse one(@PathVariable Long id) {
        return service.get(id, currentUser.get());
    }

    @GetMapping("/{id}/donors")
    @PreAuthorize("hasAnyRole('REQUESTER','ADMIN')")
    public List<DonorMatchResponse> donors(@PathVariable Long id) {
        return service.contactedDonors(id, currentUser.get());
    }

    @PostMapping("/{id}/notify-more")
    @PreAuthorize("hasRole('REQUESTER')")
    public Map<String, Object> notifyMore(@PathVariable Long id,
                                          @RequestParam(defaultValue = "50") double radiusKm) {
        int sent = service.notifyMore(id, currentUser.get(), Math.min(radiusKm, 200));
        return Map.of("newDonorsNotified", sent);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('REQUESTER')")
    public BloodRequestResponse cancel(@PathVariable Long id) {
        return service.cancel(id, currentUser.get());
    }
}
