package com.bloodconnect.repository;

import com.bloodconnect.model.Donor;
import com.bloodconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DonorRepository extends JpaRepository<Donor, Long> {
    Optional<Donor> findByUser(User user);
    List<Donor> findByAvailableTrueAndBloodGroupIn(Collection<String> groups);
}
