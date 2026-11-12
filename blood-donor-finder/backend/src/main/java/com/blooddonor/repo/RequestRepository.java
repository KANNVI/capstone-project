package com.blooddonor.repo;

import com.blooddonor.model.BloodRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByOwnerEmailOrderByIdDesc(String email);
}
