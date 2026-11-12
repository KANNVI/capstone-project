package com.bloodconnect.service;

import com.bloodconnect.dto.AdminStats;
import com.bloodconnect.dto.AdminUserResponse;
import com.bloodconnect.dto.BloodRequestResponse;
import com.bloodconnect.exception.ApiException;
import com.bloodconnect.model.*;
import com.bloodconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository users;
    private final DonorRepository donors;
    private final BloodRequestRepository requests;
    private final DonorRequestRepository donorRequests;
    private final DonationHistoryRepository history;
    private final BloodRequestService requestService;

    @Transactional(readOnly = true)
    public AdminStats stats() {
        long pending = requests.countByStatus(RequestStatus.PENDING);
        long active = pending + requests.countByStatus(RequestStatus.DONOR_ACCEPTED)
                + requests.countByStatus(RequestStatus.DONATION_IN_PROGRESS);
        Map<String, Long> byGroup = donors.findAll().stream()
                .collect(Collectors.groupingBy(Donor::getBloodGroup, TreeMap::new, Collectors.counting()));
        return new AdminStats(users.count(), users.countByRole(Role.DONOR), users.countByRole(Role.REQUESTER),
                requests.count(), pending, active, requests.countByStatus(RequestStatus.COMPLETED),
                history.count(), byGroup);
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> users() {
        return users.findAllByOrderByCreatedAtDesc().stream().map(u -> {
            String group = u.getRole() == Role.DONOR ? donors.findByUser(u).map(Donor::getBloodGroup).orElse(null) : null;
            return new AdminUserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole(), u.getCity(),
                    u.isVerified(), u.isActive(), group, u.getCreatedAt());
        }).toList();
    }

    @Transactional
    public void setVerified(Long id, boolean value) {
        User u = users.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
        u.setVerified(value);
        users.save(u);
    }

    @Transactional
    public void setActive(Long id, boolean value, User admin) {
        User u = users.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
        if (u.getId().equals(admin.getId())) throw ApiException.badRequest("You cannot block your own account");
        u.setActive(value);
        users.save(u);
    }

    @Transactional(readOnly = true)
    public List<BloodRequestResponse> requests() {
        return requests.findAllByOrderByCreatedAtDesc().stream().map(r -> requestService.toResponse(r, true)).toList();
    }

    /** Fake / inappropriate request: removed completely, unless a donation was already recorded for it. */
    @Transactional
    public String removeRequest(Long id) {
        BloodRequest br = requests.findById(id).orElseThrow(() -> ApiException.notFound("Request not found"));
        if (history.existsByBloodRequest(br)) {
            br.setStatus(RequestStatus.CANCELLED);
            requests.save(br);
            return "Request has donation records, so it was cancelled instead of deleted";
        }
        donorRequests.deleteByBloodRequest(br);
        requests.delete(br);
        return "Request deleted";
    }
}
