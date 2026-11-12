package com.bloodconnect.service;

import com.bloodconnect.model.Donor;
import com.bloodconnect.repository.DonorRepository;
import com.bloodconnect.util.BloodCompatibility;
import com.bloodconnect.util.GeoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * THE core of the project:
 *   compatible blood group  ->  available + eligible  ->  within radius  ->  sorted
 * Exact group matches come first (O- is scarce, keep it for when needed), then nearest first.
 */
@Service
public class MatchingService {

    public record Match(Donor donor, double distanceKm, boolean exact) {}

    private final DonorRepository donors;
    private final double defaultRadiusKm;
    private final int maxDonors;
    private final int minGapDays;

    public MatchingService(DonorRepository donors,
                           @Value("${bloodconnect.matching.radius-km}") double defaultRadiusKm,
                           @Value("${bloodconnect.matching.max-donors}") int maxDonors,
                           @Value("${bloodconnect.matching.min-gap-days}") int minGapDays) {
        this.donors = donors;
        this.defaultRadiusKm = defaultRadiusKm;
        this.maxDonors = maxDonors;
        this.minGapDays = minGapDays;
    }

    public double defaultRadiusKm() { return defaultRadiusKm; }
    public int maxDonors() { return maxDonors; }

    @Transactional(readOnly = true)
    public List<Match> findMatches(String recipientGroup, double lat, double lon, double radiusKm, Long excludeUserId) {
        List<String> groups = BloodCompatibility.compatibleDonorGroups(recipientGroup);
        if (groups.isEmpty()) return List.of();

        return donors.findByAvailableTrueAndBloodGroupIn(groups).stream()
                .filter(d -> d.getLatitude() != null && d.getLongitude() != null)
                .filter(d -> d.getUser().isActive())
                .filter(d -> excludeUserId == null || !d.getUser().getId().equals(excludeUserId))
                .filter(d -> d.isEligible(minGapDays))
                .map(d -> new Match(d,
                        GeoUtils.haversineKm(lat, lon, d.getLatitude(), d.getLongitude()),
                        d.getBloodGroup().equals(recipientGroup)))
                .filter(m -> m.distanceKm() <= radiusKm)
                .sorted(Comparator.comparing((Match m) -> !m.exact())
                        .thenComparingDouble(Match::distanceKm))
                .toList();
    }
}
