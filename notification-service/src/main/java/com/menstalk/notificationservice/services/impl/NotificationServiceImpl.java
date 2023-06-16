package com.menstalk.notificationservice.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.menstalk.notificationservice.models.Notification;
import com.menstalk.notificationservice.models.NotificationKey;
import com.menstalk.notificationservice.repositories.NotificationRepository;
import com.menstalk.notificationservice.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    @Override
    public Notification markAsReadById(NotificationKey notificationKey) throws JsonProcessingException {
        Notification notification = new Notification();

        notification = notificationRepository.findById(notificationKey).orElseThrow(() -> new IllegalStateException("Notification not found"));
        notification.setRead(true);

        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> markAllAsRead(Long userId) throws JsonProcessingException {
        List<Notification> notifications = new ArrayList<>();
        List<Notification> notificationsToMarkAsRead = new ArrayList<>();

        notifications = notificationRepository.findAllByNotificationKeyUserId(userId);
        notifications.forEach(x -> {
            if (!x.isRead()) {
                x.setRead(true);
                notificationsToMarkAsRead.add(x);
            }
        });
        return notificationRepository.saveAll(notificationsToMarkAsRead);
    }

}
