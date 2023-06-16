package com.menstalk.notificationservice.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstalk.notificationservice.dto.NotificationResponse;
import com.menstalk.notificationservice.mapper.NotificationConvert;
import com.menstalk.notificationservice.models.Notification;
import com.menstalk.notificationservice.models.NotificationKey;
import com.menstalk.notificationservice.repositories.NotificationRepository;
import com.menstalk.notificationservice.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final NotificationConvert notificationConvert;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    // 將用戶ID與其WebSocket會話對應
    private ConcurrentHashMap<String, WebSocketSession> userSessionMap = new ConcurrentHashMap<>();


    // 用戶ID將在連接的URL中作為參數提供。例如：ws://localhost:80/notification-service/notification?userId=1
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = session.getUri().getQuery().split("=")[1];
        userSessionMap.put(userId, session);

        List<Notification> notifications = notificationRepository.findByNotificationKeyUserIdLimit30(Long.valueOf(userId));
        if (notifications.size() > 0) {
            sendNotifications(userId, notifications);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = session.getUri().getQuery().split("=")[1];
        userSessionMap.remove(userId);
        log.info("WebSocket connection closed for user: " + userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 解析接收到的 JSON 訊息
        JsonNode jsonMessage = objectMapper.readTree(message.getPayload());
        String operation = jsonMessage.get("operation").asText();

        // 檢查訊息是不是一個已讀請求
        if ("markAsRead".equals(operation)) {
            try {
                String notificationId = jsonMessage.get("notificationId").asText();
                Long userId = jsonMessage.get("userId").asLong();
                String createTimeStr = jsonMessage.get("createTime").asText();
                LocalDateTime createTime = LocalDateTime.parse(createTimeStr);
                Notification notification = new Notification();

                NotificationKey notificationKey = new NotificationKey(userId, createTime, UUID.fromString(notificationId));
                notification = notificationService.markAsReadById(notificationKey);
                sendNotification(notification.getNotificationKey().getUserId().toString(), notification);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error while marking notification as read: " + e.getMessage());
            }
        } else if ("markAllAsRead".equals(operation)) {
            try {
                List<Notification> notificationsSend = new ArrayList<>();
                Long userId = jsonMessage.get("userId").asLong();
                notificationsSend = notificationService.markAllAsRead(userId);
                if (notificationsSend.size() > 0) {
                    sendNotifications(userId.toString(), notificationsSend);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error while marking notification as read: " + e.getMessage());
            }

        }
    }

    // 在這裡添加一個新的方法，用於發送新的通知
    public void sendNotification(String userId, Notification notification) throws JsonProcessingException {
        TextMessage message = new TextMessage(objectMapper.writeValueAsString(notificationConvert.notificationConvertToNotificationResponse(notification)));
        WebSocketSession session = userSessionMap.get(userId);

        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Error while sending notification: " + e.getMessage());
                // 嘗試關閉產生錯誤的session
                try {
                    session.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                // 從sessions列表中移除產生錯誤的session
                userSessionMap.remove(userId);
            }
        }
    }

    public void sendNotifications(String userId, List<Notification> notifications) throws JsonProcessingException {
        List<NotificationResponse> notificationResponses = new ArrayList<>();
        notificationResponses = notifications.stream()
                .map(notificationConvert::notificationConvertToNotificationResponse)
                .collect(Collectors.toList());
        TextMessage message = new TextMessage(objectMapper.writeValueAsString(notificationResponses));
        WebSocketSession session = userSessionMap.get(userId);

        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Error while sending notification: " + e.getMessage());
                // 嘗試關閉產生錯誤的session
                try {
                    session.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                // 從sessions列表中移除產生錯誤的session
                userSessionMap.remove(userId);
            }
        }
    }
}
