package com.blooddonor.controller;

import com.blooddonor.model.BloodBank;
import com.blooddonor.repo.BankRepository;
import com.blooddonor.service.MatchingService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/banks")
public class BankController {
    private final BankRepository repo;

    public BankController(BankRepository repo) { this.repo = repo; }

    @GetMapping
    public List<BloodBank> all() { return repo.findAll(); }

    /** Admin only (see SecurityConfig): adjust stock of one blood group by delta (+1 / -1). */
    @PutMapping("/{id}/stock/{group}")
    public BloodBank stock(@PathVariable Long id, @PathVariable String group, @RequestParam int delta) {
        if (!MatchingService.GROUPS.contains(group)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown blood group");
        BloodBank b = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        b.stock.put(group, Math.max(0, b.stock.getOrDefault(group, 0) + delta));
        return repo.save(b);
    }
}
