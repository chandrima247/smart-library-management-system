package com.college.slms.service;

import com.college.slms.domain.Notification;
import com.college.slms.domain.User;
import com.college.slms.domain.enums.NotificationType;
import com.college.slms.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Creates and queries in-app notifications. Other services delegate here so the
 * notification format and persistence live in a single place.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void notify(User recipient, NotificationType type, String title, String message, String linkUrl) {
        notificationRepository.save(new Notification(recipient, type, title, message, linkUrl));
    }

    @Transactional(readOnly = true)
    public List<Notification> recentFor(Long userId) {
        return notificationRepository.findTop20ByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId);
    }
}
