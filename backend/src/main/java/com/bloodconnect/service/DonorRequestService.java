package com.bloodconnect.service;

import com.bloodconnect.dto.DonorRequestResponse;
import com.bloodconnect.exception.ApiException;
import com.bloodconnect.model.*;
import com.bloodconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Everything a donor can do with an incoming request: accept / reject / start / complete. */
@Service
@RequiredArgsConstructor
public class DonorRequestService {
    private static final Set<DonorRequestStatus> COUNTS_TOWARDS_UNITS =
            EnumSet.of(DonorRequestStatus.ACCEPTED, DonorRequestStatus.IN_PROGRESS, DonorRequestStatus.COMPLETED);
    private static final Set<DonorRequestStatus> ACTIVE_OR_DONE = COUNTS_TOWARDS_UNITS;

    private final DonorRequestRepository donorRequests;
    private final BloodRequestRepository requests;
    private final DonorRepository donors;
    private final DonationHistoryRepository history;
    private final UserService userService;
    private final BloodRequestService requestService;
    private final NotificationService notifications;

    @Transactional(readOnly = true)
    public List<DonorRequestResponse> inbox(User user) {
        Donor donor = userService.donorOf(user);
        return donorRequests.findByDonorOrderByCreatedAtDesc(donor).stream().map(this::toResponse).toList();
    }

    @Transactional
    public DonorRequestResponse accept(Long id, User user) {
        DonorRequest dr = owned(id, user);
        BloodRequest br = dr.getBloodRequest();
        require(dr.getStatus() == DonorRequestStatus.SENT, "You already responded to this request");
        requireOpen(br);
        if (!dr.getDonor().isAvailable())
            throw ApiException.conflict("Set yourself as available before accepting");

        dr.setStatus(DonorRequestStatus.ACCEPTED);
        dr.setRespondedAt(LocalDateTime.now());
        donorRequests.saveAndFlush(dr);
        refreshRequestStatus(br);

        notifications.push(br.getRequester(),
                dr.getDonor().getUser().getName() + " (" + dr.getDonor().getBloodGroup() + ") accepted your request at "
                        + br.getHospital() + ". Contact details are now visible.");
        return toResponse(dr);
    }

    @Transactional
    public DonorRequestResponse reject(Long id, User user) {
        DonorRequest dr = owned(id, user);
        require(dr.getStatus() == DonorRequestStatus.SENT, "You already responded to this request");
        dr.setStatus(DonorRequestStatus.REJECTED);
        dr.setRespondedAt(LocalDateTime.now());
        donorRequests.save(dr);
        return toResponse(dr);
    }

    @Transactional
    public DonorRequestResponse start(Long id, User user) {
        DonorRequest dr = owned(id, user);
        require(dr.getStatus() == DonorRequestStatus.ACCEPTED, "Accept the request first");
        requireOpen(dr.getBloodRequest());
        dr.setStatus(DonorRequestStatus.IN_PROGRESS);
        donorRequests.saveAndFlush(dr);
        refreshRequestStatus(dr.getBloodRequest());
        notifications.push(dr.getBloodRequest().getRequester(),
                dr.getDonor().getUser().getName() + " has started the donation at " + dr.getBloodRequest().getHospital() + ".");
        return toResponse(dr);
    }

    @Transactional
    public DonorRequestResponse complete(Long id, User user) {
        DonorRequest dr = owned(id, user);
        BloodRequest br = dr.getBloodRequest();
        require(dr.getStatus() == DonorRequestStatus.ACCEPTED || dr.getStatus() == DonorRequestStatus.IN_PROGRESS,
                "This donation cannot be completed right now");
        require(br.getStatus() != RequestStatus.CANCELLED, "This request was cancelled");

        dr.setStatus(DonorRequestStatus.COMPLETED);
        donorRequests.saveAndFlush(dr);

        Donor donor = dr.getDonor();
        donor.setLastDonationDate(LocalDate.now());
        donor.setAvailable(false);   // 90-day rest; donor can switch back on after that
        donors.save(donor);

        DonationHistory h = new DonationHistory();
        h.setDonor(donor);
        h.setBloodRequest(br);
        h.setDonationDate(LocalDate.now());
        h.setUnits(1);
        history.save(h);

        br.setUnitsFulfilled(br.getUnitsFulfilled() + 1);
        refreshRequestStatus(br);

        notifications.push(br.getRequester(),
                donor.getUser().getName() + " completed a donation for you (" + br.getUnitsFulfilled() + "/"
                        + br.getUnitsRequired() + " units).");
        notifications.push(donor.getUser(), "Thank you! Your donation was recorded. You are on rest for the next 90 days.");
        return toResponse(dr);
    }

    // ------------------------------------------------------------ helpers
    /** Recomputes the overall request status from its donor responses. */
    private void refreshRequestStatus(BloodRequest br) {
        if (br.getStatus() == RequestStatus.CANCELLED) return;

        long inProgress = donorRequests.countByBloodRequestAndStatusIn(br, EnumSet.of(DonorRequestStatus.IN_PROGRESS));
        long accepted = donorRequests.countByBloodRequestAndStatusIn(br, EnumSet.of(DonorRequestStatus.ACCEPTED));
        long committed = donorRequests.countByBloodRequestAndStatusIn(br, ACTIVE_OR_DONE);

        if (br.getUnitsFulfilled() >= br.getUnitsRequired()) {
            br.setStatus(RequestStatus.COMPLETED);
        } else if (inProgress > 0) {
            br.setStatus(RequestStatus.DONATION_IN_PROGRESS);
        } else if (accepted > 0) {
            br.setStatus(RequestStatus.DONOR_ACCEPTED);
        } else {
            br.setStatus(RequestStatus.PENDING);
        }
        requests.save(br);

        // enough donors already committed -> close the remaining invitations
        if (committed >= br.getUnitsRequired() || br.getStatus() == RequestStatus.COMPLETED) {
            for (DonorRequest other : donorRequests.findByBloodRequestAndStatus(br, DonorRequestStatus.SENT)) {
                other.setStatus(DonorRequestStatus.EXPIRED);
                donorRequests.save(other);
            }
        }
    }

    private DonorRequest owned(Long id, User user) {
        Donor donor = userService.donorOf(user);
        DonorRequest dr = donorRequests.findById(id).orElseThrow(() -> ApiException.notFound("Request not found"));
        if (!dr.getDonor().getId().equals(donor.getId())) throw ApiException.forbidden("This request was not sent to you");
        return dr;
    }

    private void requireOpen(BloodRequest br) {
        if (br.getStatus() == RequestStatus.CANCELLED || br.getStatus() == RequestStatus.COMPLETED)
            throw ApiException.conflict("This request is already closed");
    }

    private void require(boolean ok, String message) {
        if (!ok) throw ApiException.conflict(message);
    }

    private DonorRequestResponse toResponse(DonorRequest dr) {
        boolean reveal = COUNTS_TOWARDS_UNITS.contains(dr.getStatus());
        return new DonorRequestResponse(dr.getId(), dr.getStatus(), dr.getDistanceKm(), dr.getCreatedAt(),
                dr.getRespondedAt(), requestService.toResponse(dr.getBloodRequest(), reveal));
    }
}
