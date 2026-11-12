package com.blooddonor.controller;

import com.blooddonor.model.*;
import com.blooddonor.repo.RequestRepository;
import com.blooddonor.service.MatchingService;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    private static final Logger log = LoggerFactory.getLogger(RequestController.class);
    private static final int SEARCH_RADIUS_KM = 100;

    private final RequestRepository repo;
    private final MatchingService matching;

    public RequestController(RequestRepository repo, MatchingService matching) { this.repo = repo; this.matching = matching; }

    @GetMapping("/mine")
    public List<BloodRequest> mine(Authentication auth) { return repo.findByOwnerEmailOrderByIdDesc(auth.getName()); }

    @PostMapping
    public BloodRequest create(@Valid @RequestBody BloodRequest r, Authentication auth) {
        if (!MatchingService.GROUPS.contains(r.bloodGroup) || !MatchingService.CITY.containsKey(r.city))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown blood group or city");
        r.id = null; r.status = "OPEN"; r.verified = false; r.ownerEmail = auth.getName();
        r.alerted = new HashSet<>(); r.accepted = new HashSet<>();
        return repo.save(r);
    }

    @GetMapping("/{id}/matches")
    public List<DonorView> matches(@PathVariable Long id, Authentication auth) {
        BloodRequest r = owned(id, auth);
        return matching.match(r.bloodGroup, r.city, SEARCH_RADIUS_KM, r);
    }

    /** Notify one matching donor. Replace the log line with SMS/email/push (Twilio, Firebase, JavaMail). */
    @PostMapping("/{id}/alert/{donorId}")
    public BloodRequest alert(@PathVariable Long id, @PathVariable Long donorId, Authentication auth) {
        BloodRequest r = owned(id, auth);
        boolean isMatch = matching.match(r.bloodGroup, r.city, SEARCH_RADIUS_KM, r).stream().anyMatch(v -> v.id().equals(donorId));
        if (!isMatch) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Donor is not a match for this request");
        r.alerted.add(donorId);
        log.info("ALERT: donor {} notified about request {}", donorId, id);
        return repo.save(r);
    }

    @PostMapping("/{id}/alert-all")
    public BloodRequest alertAll(@PathVariable Long id, Authentication auth) {
        BloodRequest r = owned(id, auth);
        matching.match(r.bloodGroup, r.city, SEARCH_RADIUS_KM, r).forEach(v -> r.alerted.add(v.id()));
        return repo.save(r);
    }

    /** Demo: the requester simulates the donor accepting. In production the donor accepts from their own account. */
    @PostMapping("/{id}/accept/{donorId}")
    public BloodRequest accept(@PathVariable Long id, @PathVariable Long donorId, Authentication auth) {
        BloodRequest r = owned(id, auth);
        if (!r.alerted.contains(donorId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Alert the donor first");
        r.accepted.add(donorId);
        return repo.save(r);
    }

    @PostMapping("/{id}/status")
    public BloodRequest status(@PathVariable Long id, @RequestParam String value, Authentication auth) {
        if (!Set.of("FULFILLED", "CLOSED").contains(value))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
        BloodRequest r = owned(id, auth);
        r.status = value;
        return repo.save(r);
    }

    private BloodRequest owned(Long id, Authentication auth) {
        BloodRequest r = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !auth.getName().equals(r.ownerEmail)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return r;
    }
}
