package com.bloodconnect.service;

import com.bloodconnect.dto.BloodRequestCreate;
import com.bloodconnect.dto.BloodRequestResponse;
import com.bloodconnect.dto.DonorMatchResponse;
import com.bloodconnect.exception.ApiException;
import com.bloodconnect.model.*;
import com.bloodconnect.repository.BloodRequestRepository;
import com.bloodconnect.repository.DonorRequestRepository;
import com.bloodconnect.service.MatchingService.Match;
import com.bloodconnect.util.BloodCompatibility;
import com.bloodconnect.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BloodRequestService {
    private static final Set<DonorRequestStatus> ACCEPTED_STATES =
            EnumSet.of(DonorRequestStatus.ACCEPTED, DonorRequestStatus.IN_PROGRESS, DonorRequestStatus.COMPLETED);

    private final BloodRequestRepository requests;
    private final DonorRequestRepository donorRequests;
    private final MatchingService matching;
    private final NotificationService notifications;

    // ---------------------------------------------------------------- create
    @Transactional
    public BloodRequestResponse create(BloodRequestCreate d, User requester) {
        if (!BloodCompatibility.isValid(d.bloodGroup()))
            throw ApiException.badRequest("Select a valid blood group");

        BloodRequest br = new BloodRequest();
        br.setRequester(requester);
        br.setPatientName(d.patientName());
        br.setBloodGroup(d.bloodGroup());
        br.setUnitsRequired(d.unitsRequired());
        br.setHospital(d.hospital().trim());
        br.setCity(d.city());
        br.setLatitude(d.latitude());
        br.setLongitude(d.longitude());
        br.setUrgency(d.urgency());
        br.setContactPhone(d.contactPhone().trim());
        br.setNotes(d.notes());
        br.setStatus(RequestStatus.PENDING);
        requests.save(br);

        List<Match> matches = matching.findMatches(br.getBloodGroup(), br.getLatitude(), br.getLongitude(),
                matching.defaultRadiusKm(), requester.getId());
        notifyDonors(br, matches);
        return toResponse(br, true);
    }

    /** Creates a DonorRequest + in-app notification for the best donors not contacted yet. */
    private int notifyDonors(BloodRequest br, List<Match> matches) {
        int sent = 0;
        for (Match m : matches) {
            if (sent >= matching.maxDonors()) break;
            if (donorRequests.existsByBloodRequestAndDonor(br, m.donor())) continue;

            DonorRequest dr = new DonorRequest();
            dr.setBloodRequest(br);
            dr.setDonor(m.donor());
            dr.setStatus(DonorRequestStatus.SENT);
            dr.setDistanceKm(GeoUtils.round1(m.distanceKm()));
            donorRequests.save(dr);

            notifications.push(m.donor().getUser(),
                    br.getUrgency() + " request: " + br.getBloodGroup() + " blood needed at "
                            + br.getHospital() + " (" + GeoUtils.round1(m.distanceKm()) + " km away).");
            sent++;
        }
        return sent;
    }

    // ---------------------------------------------------------------- read
    @Transactional(readOnly = true)
    public List<BloodRequestResponse> listMine(User requester) {
        return requests.findByRequesterOrderByCreatedAtDesc(requester).stream()
                .map(r -> toResponse(r, true)).toList();
    }

    @Transactional(readOnly = true)
    public BloodRequestResponse get(Long id, User user) {
        return toResponse(owned(id, user), true);
    }

    /** Donors this request was sent to, nearest first, phone number only after they accepted. */
    @Transactional(readOnly = true)
    public List<DonorMatchResponse> contactedDonors(Long id, User user) {
        BloodRequest br = owned(id, user);
        return donorRequests.findByBloodRequest(br).stream()
                .sorted(Comparator.comparing(
                        (DonorRequest x) -> x.getDistanceKm() == null ? 9999.0 : x.getDistanceKm()))
                .map(dr -> {
                    Donor d = dr.getDonor();
                    boolean reveal = ACCEPTED_STATES.contains(dr.getStatus());
                    return new DonorMatchResponse(dr.getId(), d.getId(), d.getUser().getName(), d.getBloodGroup(),
                            dr.getDistanceKm(), d.getBloodGroup().equals(br.getBloodGroup()),
                            dr.getStatus(), reveal ? d.getUser().getPhone() : null);
                }).toList();
    }

    // ---------------------------------------------------------------- actions
    /** Requester presses "Notify more donors": wider radius, only donors not contacted yet. */
    @Transactional
    public int notifyMore(Long id, User user, double radiusKm) {
        BloodRequest br = owned(id, user);
        if (br.getStatus() == RequestStatus.COMPLETED || br.getStatus() == RequestStatus.CANCELLED)
            throw ApiException.conflict("This request is already closed");
        List<Match> matches = matching.findMatches(br.getBloodGroup(), br.getLatitude(), br.getLongitude(),
                radiusKm, br.getRequester().getId());
        return notifyDonors(br, matches);
    }

    @Transactional
    public BloodRequestResponse cancel(Long id, User user) {
        BloodRequest br = owned(id, user);
        if (br.getStatus() == RequestStatus.COMPLETED)
            throw ApiException.conflict("A completed request cannot be cancelled");
        br.setStatus(RequestStatus.CANCELLED);
        requests.save(br);
        for (DonorRequest dr : donorRequests.findByBloodRequest(br)) {
            if (dr.getStatus() == DonorRequestStatus.SENT || dr.getStatus() == DonorRequestStatus.ACCEPTED) {
                dr.setStatus(DonorRequestStatus.EXPIRED);
                donorRequests.save(dr);
                notifications.push(dr.getDonor().getUser(),
                        "The " + br.getBloodGroup() + " request at " + br.getHospital() + " was cancelled.");
            }
        }
        return toResponse(br, true);
    }

    // ---------------------------------------------------------------- helpers
    private BloodRequest owned(Long id, User user) {
        BloodRequest br = requests.findById(id).orElseThrow(() -> ApiException.notFound("Request not found"));
        if (user.getRole() != Role.ADMIN && !br.getRequester().getId().equals(user.getId()))
            throw ApiException.forbidden("This is not your request");
        return br;
    }

    /** @param revealPhone false -> contact phone hidden (donor sees it only after accepting) */
    public BloodRequestResponse toResponse(BloodRequest br, boolean revealPhone) {
        return new BloodRequestResponse(br.getId(), br.getRequester().getName(), br.getPatientName(),
                br.getBloodGroup(), br.getUnitsRequired(), br.getUnitsFulfilled(), br.getHospital(), br.getCity(),
                br.getLatitude(), br.getLongitude(), br.getUrgency(), br.getStatus(),
                revealPhone ? br.getContactPhone() : null, br.getNotes(), br.getCreatedAt(),
                donorRequests.countByBloodRequest(br),
                donorRequests.countByBloodRequestAndStatusIn(br, ACCEPTED_STATES));
    }
}
