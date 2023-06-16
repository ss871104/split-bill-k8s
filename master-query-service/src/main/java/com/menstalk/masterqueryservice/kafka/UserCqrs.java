package com.menstalk.masterqueryservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstalk.masterqueryservice.kafka.event.AddUserEvent;
import com.menstalk.masterqueryservice.models.Status;
import com.menstalk.masterqueryservice.models.User;
import com.menstalk.masterqueryservice.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserCqrs {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "addUserTopic")
    public void addUserEvent(String addUserEventString) throws JsonProcessingException {
        AddUserEvent addUserEvent = objectMapper.readValue(addUserEventString, AddUserEvent.class);
        log.info("Received user cqrs from addUserEvent, username: {}", addUserEvent.getUsername());

        User user = new User();

        user = User.builder()
                .userId(addUserEvent.getUserId())
                .username(addUserEvent.getUsername())
                .name(addUserEvent.getName())
                .registerTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .userStatus(Status.ACTIVE)
                .build();
        try {
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("User cqrs from addUserEvent failed, username: {} add failed, error: {}", addUserEvent.getUsername(), e.toString());
        }

    }

    @KafkaListener(topics = "disableUserTopic")
    public void disableUserEvent(String username) {
        log.info("Received user cqrs from disableUserEvent, username: {}", username);
        User user = new User();

        user = userRepository.findByUsername(username);
        user.setUserStatus(Status.DISABLED);

        try {
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("User cqrs from disableUserEvent failed, username: {} disable failed, error: {}", username, e.toString());
        }
    }

    @KafkaListener(topics = "updateUserNameTopic")
    public void updateUserNameEvent(String userMessage) throws JsonProcessingException {
        User user = objectMapper.readValue(userMessage, User.class);
        log.info("Received user cqrs from updateUserNameEvent, username: {}", user.getUsername());
        try {
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("User cqrs from updateUserNameEvent failed, username: {}'s name update failed, error: {}", user.getUsername(), e.toString());
        }

    }
}
