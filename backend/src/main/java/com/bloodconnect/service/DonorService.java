package com.bloodconnect.service;

import com.bloodconnect.dto.DonationResponse;
import com.bloodconnect.dto.DonorSearchResult;
import com.bloodconnect.exception.ApiException;
import com.bloodconnect.model.Donor;
import com.bloodconnect.model.User;
import com.bloodconnect.repository.DonationHistoryRepository;
import com.bloodconnect.util.BloodCompatibility;
import com.bloodconnect.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DonorService {
    private final DonationHistoryRepository history;
    private final UserService userService;
    private final MatchingService matching;

    @Transactional(readOnly = true)
    public List<DonationResponse> history(User user) {
        Donor donor = userService.donorOf(user);
        return history.findByDonorOrderByDonationDateDescIdDesc(donor).stream()
                .map(h -> new DonationResponse(h.getId(), h.getDonationDate(), h.getUnits(),
                        h.getBloodRequest() == null ? null : h.getBloodRequest().getHospital(),
                        h.getBloodRequest() == null ? null : h.getBloodRequest().getBloodGroup()))
                .toList();
    }

    /** Quick search for the requester's "Find donors" screen (before / without creating a request). */
    @Transactional(readOnly = true)
    public List<DonorSearchResult> search(String bloodGroup, double lat, double lon, Double radiusKm, User caller) {
        if (!BloodCompatibility.isValid(bloodGroup)) throw ApiException.badRequest("Invalid blood group");
        double radius = radiusKm == null ? matching.defaultRadiusKm() : Math.min(radiusKm, 200);
        return matching.findMatches(bloodGroup, lat, lon, radius, caller.getId()).stream()
                .map(m -> new DonorSearchResult(m.donor().getId(), m.donor().getUser().getName(),
                        m.donor().getBloodGroup(), GeoUtils.round1(m.distanceKm()), m.exact()))
                .toList();
    }
}
