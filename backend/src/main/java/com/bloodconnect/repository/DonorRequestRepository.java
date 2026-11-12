package com.bloodconnect.repository;

import com.bloodconnect.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DonorRequestRepository extends JpaRepository<DonorRequest, Long> {
    List<DonorRequest> findByDonorOrderByCreatedAtDesc(Donor donor);
    List<DonorRequest> findByBloodRequest(BloodRequest request);
    List<DonorRequest> findByBloodRequestAndStatus(BloodRequest request, DonorRequestStatus status);
    boolean existsByBloodRequestAndDonor(BloodRequest request, Donor donor);
    long countByBloodRequest(BloodRequest request);
    long countByBloodRequestAndStatusIn(BloodRequest request, Collection<DonorRequestStatus> statuses);
    void deleteByBloodRequest(BloodRequest request);
}
