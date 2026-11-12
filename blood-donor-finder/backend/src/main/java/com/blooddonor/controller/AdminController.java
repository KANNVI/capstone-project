package com.blooddonor.controller;

import com.blooddonor.model.*;
import com.blooddonor.repo.*;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final DonorRepository donors;
    private final RequestRepository requests;

    public AdminController(DonorRepository donors, RequestRepository requests) { this.donors = donors; this.requests = requests; }

    @GetMapping("/donors")
    public List<Donor> allDonors() { return donors.findAll(); }

    @GetMapping("/requests")
    public List<BloodRequest> allRequests() { return requests.findAll(); }

    @PostMapping("/donors/{id}/verify")
    public Donor verifyDonor(@PathVariable Long id, @RequestParam boolean value) {
        Donor d = donors.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        d.verified = value;
        return donors.save(d);
    }

    @PostMapping("/requests/{id}/verify")
    public BloodRequest verifyRequest(@PathVariable Long id, @RequestParam boolean value) {
        BloodRequest r = requests.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        r.verified = value;
        return requests.save(r);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        var all = requests.findAll();
        return Map.of(
                "donors", donors.count(),
                "eligibleDonors", donors.findAll().stream().filter(Donor::isEligible).count(),
                "openRequests", all.stream().filter(r -> r.status.equals("OPEN")).count(),
                "fulfilledRequests", all.stream().filter(r -> r.status.equals("FULFILLED")).count());
    }
}
