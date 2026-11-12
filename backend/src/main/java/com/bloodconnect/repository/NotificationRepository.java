package com.bloodconnect.repository;

import com.bloodconnect.model.Notification;
import com.bloodconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop50ByUserOrderByCreatedAtDescIdDesc(User user);
    List<Notification> findByUserAndSeenFalse(User user);
    long countByUserAndSeenFalse(User user);
}
