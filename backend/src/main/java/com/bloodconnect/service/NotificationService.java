package com.bloodconnect.service;

import com.bloodconnect.dto.NotificationResponse;
import com.bloodconnect.model.Notification;
import com.bloodconnect.model.User;
import com.bloodconnect.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notifications;

    @Transactional
    public void push(User user, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        notifications.save(n);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(User user) {
        return notifications.findTop50ByUserOrderByCreatedAtDescIdDesc(user).stream()
                .map(n -> new NotificationResponse(n.getId(), n.getMessage(), n.isSeen(), n.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(User user) {
        return notifications.countByUserAndSeenFalse(user);
    }

    @Transactional
    public void markAllRead(User user) {
        List<Notification> unread = notifications.findByUserAndSeenFalse(user);
        unread.forEach(n -> n.setSeen(true));
        notifications.saveAll(unread);
    }
}
