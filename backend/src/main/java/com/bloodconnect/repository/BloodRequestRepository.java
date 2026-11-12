package com.bloodconnect.repository;

import com.bloodconnect.model.BloodRequest;
import com.bloodconnect.model.RequestStatus;
import com.bloodconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByRequesterOrderByCreatedAtDesc(User requester);
    List<BloodRequest> findAllByOrderByCreatedAtDesc();
    long countByStatus(RequestStatus status);
}
