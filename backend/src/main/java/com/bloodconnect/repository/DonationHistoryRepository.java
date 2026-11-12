package com.bloodconnect.repository;

import com.bloodconnect.model.BloodRequest;
import com.bloodconnect.model.DonationHistory;
import com.bloodconnect.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationHistoryRepository extends JpaRepository<DonationHistory, Long> {
    List<DonationHistory> findByDonorOrderByDonationDateDescIdDesc(Donor donor);
    boolean existsByBloodRequest(BloodRequest request);
}
