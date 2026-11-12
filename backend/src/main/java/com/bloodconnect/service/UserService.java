package com.bloodconnect.service;

import com.bloodconnect.dto.DonorProfileUpdate;
import com.bloodconnect.dto.ProfileResponse;
import com.bloodconnect.exception.ApiException;
import com.bloodconnect.model.Donor;
import com.bloodconnect.model.Role;
import com.bloodconnect.model.User;
import com.bloodconnect.repository.DonorRepository;
import com.bloodconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UserService {
    private final DonorRepository donors;
    private final UserRepository users;
    private final int minGapDays;

    public UserService(DonorRepository donors, UserRepository users,
                       @Value("${bloodconnect.matching.min-gap-days}") int minGapDays) {
        this.donors = donors;
        this.users = users;
        this.minGapDays = minGapDays;
    }

    @Transactional(readOnly = true)
    public ProfileResponse toProfile(User u) {
        if (u.getRole() == Role.DONOR) {
            Donor d = donors.findByUser(u).orElse(null);
            if (d != null) {
                LocalDate next = d.getLastDonationDate() == null ? null : d.getLastDonationDate().plusDays(minGapDays);
                return new ProfileResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole(), u.getCity(),
                        u.isVerified(), d.getBloodGroup(), d.isAvailable(), d.getLastDonationDate(), next,
                        d.isEligible(minGapDays), d.getLatitude(), d.getLongitude());
            }
        }
        return new ProfileResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole(), u.getCity(),
                u.isVerified(), null, null, null, null, false, null, null);
    }

    public Donor donorOf(User u) {
        if (u.getRole() != Role.DONOR) throw ApiException.forbidden("Only donors can do this");
        return donors.findByUser(u).orElseThrow(() -> ApiException.notFound("Donor profile not found"));
    }

    @Transactional
    public ProfileResponse updateDonorProfile(User u, DonorProfileUpdate req) {
        Donor d = donorOf(u);
        if (req.available() != null) d.setAvailable(req.available());
        if (req.lastDonationDate() != null) {
            if (req.lastDonationDate().isAfter(LocalDate.now()))
                throw ApiException.badRequest("Last donation date cannot be in the future");
            d.setLastDonationDate(req.lastDonationDate());
        }
        if (req.latitude() != null && req.longitude() != null) {
            d.setLatitude(req.latitude());
            d.setLongitude(req.longitude());
        }
        if (req.city() != null && !req.city().isBlank()) u.setCity(req.city().trim());
        if (req.phone() != null && !req.phone().isBlank()) u.setPhone(req.phone().trim());
        users.save(u);
        donors.save(d);
        return toProfile(u);
    }

    @Transactional
    public ProfileResponse setAvailability(User u, boolean available) {
        Donor d = donorOf(u);
        d.setAvailable(available);
        donors.save(d);
        return toProfile(u);
    }
}
