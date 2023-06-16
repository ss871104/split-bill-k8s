package com.menstalk.notificationservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstalk.notificationservice.dto.MemberResponse;
import com.menstalk.notificationservice.dto.PartyResponse;
import com.menstalk.notificationservice.kafka.event.NewBillEvent;
import com.menstalk.notificationservice.kafka.event.NewMemberEvent;
import com.menstalk.notificationservice.kafka.event.NewUserEvent;
import com.menstalk.notificationservice.models.Notification;
import com.menstalk.notificationservice.models.NotificationKey;
import com.menstalk.notificationservice.models.NotificationType;
import com.menstalk.notificationservice.proxy.MasterQueryProxy;
import com.menstalk.notificationservice.repositories.NotificationRepository;
import com.menstalk.notificationservice.websocket.NotificationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationListener {
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;
    private final MasterQueryProxy masterQueryProxy;
    private final NotificationHandler notificationHandler;

    @KafkaListener(topics = "newUserTopic")
    public void newUserEvent(String newUserEventString) throws JsonProcessingException {
        NewUserEvent newUserEvent = objectMapper.readValue(newUserEventString, NewUserEvent.class);
        log.info("Received newUserEvent, new user: {} with userId: {}", newUserEvent.getName(), newUserEvent.getUserId());

        try {
            Notification notification = new Notification();
            notification = Notification.builder()
                            .notificationKey(new NotificationKey(newUserEvent.getUserId(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), UUID.randomUUID()))
                            .title(NotificationType.NEW_USER.getTitle())
                            .content(NotificationType.NEW_USER.getContent().formatted(newUserEvent.getName()))
                            .isRead(false)
                            .build();
            notification = notificationRepository.save(notification);
            notificationHandler.sendNotification(notification.getNotificationKey().getUserId().toString(), notification);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed adding notification from newUserEvent, userId: {}, error: {}", newUserEvent.getUserId(), e.toString());
        }

    }

    @KafkaListener(topics = "newMemberTopic")
    public void newMemberEvent(String newMemberEventString) throws JsonProcessingException {
        NewMemberEvent newMemberEvent = objectMapper.readValue(newMemberEventString, NewMemberEvent.class);

        String memberName = newMemberEvent.getMemberName();
        Long partyId = newMemberEvent.getPartyId();
        List<Long> userIds = objectMapper.readValue(newMemberEvent.getUserIds(), new TypeReference<List<Long>>(){});
        log.info("Received newMemberTopic, new member {} to partyId: {}", memberName, partyId);
        try {
            List<Notification> notifications = new ArrayList<>();
            notifications = userIds.stream()
                            .map(x -> Notification.builder()
                                    .notificationKey(new NotificationKey(x, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), UUID.randomUUID()))
                                    .title(NotificationType.JOIN.getTitle())
                                    .content(NotificationType.JOIN.getContent().formatted(memberName))
                                    .isRead(false)
                                    .build())
                            .collect(Collectors.toList());
            notifications = notificationRepository.saveAll(notifications);
            notifications.forEach(x -> {
                try {
                    notificationHandler.sendNotification(x.getNotificationKey().getUserId().toString(), x);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed adding notification from newMemberEvent, new member {}, partyId: {}, error: {}", memberName, partyId, e.toString());
        }

    }

    @KafkaListener(topics = "newBillTopic")
    public void newBillEvent(String newBillEventString) throws JsonProcessingException {
        NewBillEvent newBillEvent = objectMapper.readValue(newBillEventString, NewBillEvent.class);

        try {
            List<Long> userIds = new ArrayList<>();
            List<MemberResponse> memberResponses = new ArrayList<>();
            PartyResponse partyResponse = new PartyResponse();
            List<Notification> notifications = new ArrayList<>();
            String partyName = "";

            try {
                log.info("Retrieved getMembersByPartyId feign");
                memberResponses = masterQueryProxy.getMembersByPartyId(newBillEvent.getPartyId()).getBody();
            } catch (Exception e) {
                log.error("getMembersByPartyId feign error, error: {}", e.toString());
            }
            try {
                log.info("Retrieved getPartyById feign");
                partyResponse = masterQueryProxy.getPartyById(newBillEvent.getPartyId()).getBody();
            } catch (Exception e) {
                log.error("getPartyById feign error, error: {}", e.toString());
            }

            if (partyResponse != null) {
                partyName = partyResponse.getPartyName();
            }
            final var finalPartyName = partyName;

            log.info("Received newBillEvent, new bill for partyId: {}, partyName: {}", newBillEvent.getPartyId(), partyName);

            if (memberResponses != null) {
                userIds = memberResponses.stream()
                        .map(MemberResponse::getUserId)
                        .collect(Collectors.toList());
            }

            notifications = userIds.stream()
                                .map(x -> Notification.builder()
                                        .notificationKey(new NotificationKey(x, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), UUID.randomUUID()))
                                        .title(NotificationType.BILL_ADD.getTitle())
                                        .content(NotificationType.BILL_ADD.getContent().formatted(finalPartyName))
                                        .isRead(false)
                                        .build())
                                .collect(Collectors.toList());
            notifications = notificationRepository.saveAll(notifications);
            notifications.forEach(x -> {
                try {
                    notificationHandler.sendNotification(x.getNotificationKey().getUserId().toString(), x);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed adding notification from newBillEvent, partyId: {}, error: {}", newBillEvent.getPartyId(), e.toString());
        }

    }

    @KafkaListener(topics = "weeklyOwedTopic")
    public void weeklyOwedEvent(String notificationsMessage) throws JsonProcessingException {
        List<Notification> notifications = objectMapper.readValue(notificationsMessage, new TypeReference<List<Notification>>(){});
        log.info("Received weeklyOwedTopic");
        try {
            notifications = notificationRepository.saveAll(notifications);

            notifications.forEach(x -> {
                try {
                    notificationHandler.sendNotification(x.getNotificationKey().getUserId().toString(), x);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed adding notification from weeklyOwedTopic, error: {}", e.toString());
        }
    }

}
