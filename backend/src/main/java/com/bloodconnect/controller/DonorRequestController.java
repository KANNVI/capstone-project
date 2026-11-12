package com.bloodconnect.controller;

import com.bloodconnect.dto.DonorRequestResponse;
import com.bloodconnect.security.CurrentUser;
import com.bloodconnect.service.DonorRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donor-requests")
@PreAuthorize("hasRole('DONOR')")
@RequiredArgsConstructor
public class DonorRequestController {
    private final CurrentUser currentUser;
    private final DonorRequestService service;

    @GetMapping("/mine")
    public List<DonorRequestResponse> mine() { return service.inbox(currentUser.get()); }

    @PostMapping("/{id}/accept")
    public DonorRequestResponse accept(@PathVariable Long id) { return service.accept(id, currentUser.get()); }

    @PostMapping("/{id}/reject")
    public DonorRequestResponse reject(@PathVariable Long id) { return service.reject(id, currentUser.get()); }

    @PostMapping("/{id}/start")
    public DonorRequestResponse start(@PathVariable Long id) { return service.start(id, currentUser.get()); }

    @PostMapping("/{id}/complete")
    public DonorRequestResponse complete(@PathVariable Long id) { return service.complete(id, currentUser.get()); }
}
