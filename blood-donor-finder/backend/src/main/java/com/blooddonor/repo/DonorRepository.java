package com.blooddonor.repo;

import com.blooddonor.model.Donor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonorRepository extends JpaRepository<Donor, Long> {
    List<Donor> findByOwnerEmail(String email);
}
