package com.blooddonor.service;

import com.blooddonor.model.*;
import com.blooddonor.repo.DonorRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Blood group compatibility, eligibility filtering and nearest-first ranking. */
@Service
public class MatchingService {
    public static final List<String> GROUPS = List.of("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+");

    /** donor group -> recipient groups that can receive its red cells */
    private static final Map<String, Set<String>> CAN_GIVE_TO = Map.of(
            "O-", Set.copyOf(GROUPS),
            "O+", Set.of("O+", "A+", "B+", "AB+"),
            "A-", Set.of("A-", "A+", "AB-", "AB+"),
            "A+", Set.of("A+", "AB+"),
            "B-", Set.of("B-", "B+", "AB-", "AB+"),
            "B+", Set.of("B+", "AB+"),
            "AB-", Set.of("AB-", "AB+"),
            "AB+", Set.of("AB+"));

    public static final Map<String, double[]> CITY = Map.of(
            "Gurugram", new double[]{28.4595, 77.0266},
            "Delhi", new double[]{28.6139, 77.2090},
            "Noida", new double[]{28.5355, 77.3910},
            "Faridabad", new double[]{28.4089, 77.3178},
            "Ghaziabad", new double[]{28.6692, 77.4538},
            "Chandigarh", new double[]{30.7333, 76.7794});

    private record Hit(Donor donor, int km) {}

    private final DonorRepository donors;

    public MatchingService(DonorRepository donors) { this.donors = donors; }

    public static boolean canDonate(String donorGroup, String recipientGroup) {
        return CAN_GIVE_TO.getOrDefault(donorGroup, Set.of()).contains(recipientGroup);
    }

    static int km(double[] a, double[] b) {
        double r = Math.PI / 180, dLat = (b[0] - a[0]) * r, dLon = (b[1] - a[1]) * r;
        double h = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(a[0] * r) * Math.cos(b[0] * r) * Math.pow(Math.sin(dLon / 2), 2);
        return (int) Math.round(12742 * Math.asin(Math.sqrt(h)));
    }

    /** Eligible, compatible donors within radiusKm of city, nearest first. req may be null. */
    public List<DonorView> match(String group, String city, double radiusKm, BloodRequest req) {
        double[] origin = CITY.get(city);
        if (origin == null || !GROUPS.contains(group))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown city or blood group");
        return donors.findAll().stream()
                .filter(d -> d.isEligible() && canDonate(d.bloodGroup, group))
                .map(d -> new Hit(d, km(origin, CITY.getOrDefault(d.city, origin))))
                .filter(h -> h.km() <= radiusKm)
                .sorted(Comparator.comparingInt(Hit::km))
                .map(h -> DonorView.of(h.donor(), h.km(), req))
                .toList();
    }
}
