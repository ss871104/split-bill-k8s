package com.menstalk.mastercommandservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstalk.mastercommandservice.handler.CustomException;
import com.menstalk.mastercommandservice.models.Status;
import com.menstalk.mastercommandservice.models.User;
import com.menstalk.mastercommandservice.repositories.UserRepository;
import com.menstalk.mastercommandservice.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = "userService")
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @CacheEvict(value = "user", key = "#username")
    public void disableUser(String username) {
        try {
            User user = new User();

            user = userRepository.findByUsername(username);
            if (user == null) {
                throw new CustomException("username not found");
            }
            user.setUserStatus(Status.DISABLED);
            try {
                userRepository.save(user);
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            String userJson = objectMapper.writeValueAsString(user);

            try {
                log.info("Send disableUserTopic");
                kafkaTemplate.send("disableUserTopic", userJson);
            } catch (Exception e) {
                log.error("Failed sending disableUserTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR");
            }
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    @CacheEvict(value = "user", key = "#username")
    public void updateName(String username, String name) {
        try {
            User user = new User();

            user = userRepository.findByUsername(username);
            if (user == null) {
                throw new CustomException("username not found");
            }
            user.setName(name);
            try {
                userRepository.save(user);
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            String userJson = objectMapper.writeValueAsString(user);

            try {
                log.info("Send updateUserNameTopic");
                kafkaTemplate.send("updateUserNameTopic", userJson);
            } catch (Exception e) {
                log.error("Failed sending updateUserNameTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR");
            }
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }

    }
}
