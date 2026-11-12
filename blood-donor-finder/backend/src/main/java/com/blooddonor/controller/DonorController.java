package com.blooddonor.controller;

import com.blooddonor.model.*;
import com.blooddonor.repo.DonorRepository;
import com.blooddonor.service.MatchingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/donors")
public class DonorController {
    private final DonorRepository repo;
    private final MatchingService matching;

    public DonorController(DonorRepository repo, MatchingService matching) { this.repo = repo; this.matching = matching; }

    /** Public search: eligible, compatible donors nearest first, phone masked. */
    @GetMapping("/search")
    public List<DonorView> search(@RequestParam String group, @RequestParam String city,
                                  @RequestParam(defaultValue = "50") double radius) {
        return matching.match(group, city, radius, null);
    }

    @PostMapping
    public Donor register(@Valid @RequestBody Donor d, Authentication auth) {
        if (!MatchingService.GROUPS.contains(d.bloodGroup) || !MatchingService.CITY.containsKey(d.city))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown blood group or city");
        d.id = null; d.verified = false; d.ownerEmail = auth.getName();
        return repo.save(d);
    }

    @GetMapping("/mine")
    public List<Donor> mine(Authentication auth) { return repo.findByOwnerEmail(auth.getName()); }

    @PatchMapping("/{id}/availability")
    public Donor toggle(@PathVariable Long id, Authentication auth) {
        Donor d = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!auth.getName().equals(d.ownerEmail)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        d.available = !d.available;
        return repo.save(d);
    }
}
