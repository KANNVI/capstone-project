package com.blooddonor.repo;

import com.blooddonor.model.BloodBank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<BloodBank, Long> {}
