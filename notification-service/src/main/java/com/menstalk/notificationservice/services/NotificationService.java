package com.menstalk.notificationservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.menstalk.notificationservice.models.Notification;
import com.menstalk.notificationservice.models.NotificationKey;

import java.util.List;

public interface NotificationService {
    Notification markAsReadById(NotificationKey notificationKey) throws JsonProcessingException;
    List<Notification> markAllAsRead(Long userId) throws JsonProcessingException;
}
