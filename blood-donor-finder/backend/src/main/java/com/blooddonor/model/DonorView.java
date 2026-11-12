package com.blooddonor.model;

import java.time.LocalDate;

/** Public view of a donor: phone is masked until the donor has accepted the request. */
public record DonorView(Long id, String name, String bloodGroup, String city, int distanceKm,
                        LocalDate lastDonation, String phone, boolean alerted, boolean accepted) {
    public static DonorView of(Donor d, int km, BloodRequest r) {
        boolean alerted = r != null && r.alerted.contains(d.id);
        boolean accepted = r != null && r.accepted.contains(d.id);
        String phone = accepted ? d.phone : d.phone.substring(0, 2) + "******" + d.phone.substring(8);
        return new DonorView(d.id, d.name, d.bloodGroup, d.city, km, d.lastDonation, phone, alerted, accepted);
    }
}
