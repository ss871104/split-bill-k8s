package com.menstalk.authservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstalk.authservice.dto.*;
import com.menstalk.authservice.kafka.event.AddUserEvent;
import com.menstalk.authservice.kafka.event.NewUserEvent;
import com.menstalk.authservice.services.AuthService;
import com.menstalk.authservice.handler.CustomException;
import com.menstalk.authservice.jwt.JwtUtil;
import com.menstalk.authservice.mapper.AuthConvert;
import com.menstalk.authservice.models.AccountStatus;
import com.menstalk.authservice.models.Auth;
import com.menstalk.authservice.repositories.AuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = "authService")
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthConvert authConvert;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Caching(
            evict = {@CacheEvict(value = "AuthList", allEntries = true)}
    )
    public TokenResponse register(RegisterRequest registerRequest) {
        try {
            Auth auth = new Auth();
            AddUserEvent addUserEvent = new AddUserEvent();
            NewUserEvent newUserEvent = new NewUserEvent();
            String jwtToken = "";

            try {
                auth = authRepository.findByUsername(registerRequest.getUsername());
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            if (auth == null) {
                auth = Auth.builder()
                        .name(registerRequest.getName())
                        .username(registerRequest.getUsername())
                        .password(passwordEncoder.encode(registerRequest.getPassword()))
                        .status(AccountStatus.ACTIVE)
                        .build();
                auth = authRepository.save(auth);
                authRepository.flush();
            } else {
                if (auth.getStatus().equals(AccountStatus.DISABLED)) {
                    auth = Auth.builder()
                            .authId(auth.getAuthId())
                            .name(registerRequest.getName())
                            .username(registerRequest.getUsername())
                            .password(passwordEncoder.encode(registerRequest.getPassword()))
                            .status(AccountStatus.ACTIVE)
                            .build();
                    auth = authRepository.save(auth);
                } else {
                    throw new CustomException("username duplicate!");
                }
            }
            addUserEvent = AddUserEvent.builder()
                            .userId(auth.getAuthId())
                            .username(auth.getUsername())
                            .name(auth.getName())
                            .build();
            newUserEvent = NewUserEvent.builder()
                    .userId(auth.getAuthId())
                    .name(auth.getName())
                    .build();

            String addUserEventString = objectMapper.writeValueAsString(addUserEvent);
            String newUserEventString = objectMapper.writeValueAsString(newUserEvent);

            try {
                log.info("Send addUserTopic");
                kafkaTemplate.send("addUserTopic", addUserEventString);
            } catch (Exception e) {
                log.error("Failed sending addUserTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR!");
            }
            try {
                log.info("Send newUserTopic");
                kafkaTemplate.send("newUserTopic", newUserEventString);
            } catch (Exception e) {
                log.error("Failed sending newUserTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR!");
            }

            jwtToken = jwtUtil.generateToken(authConvert.authConvertToAuthResponse(auth));
            
            redisTemplate.opsForValue().set("jwt::" + registerRequest.getUsername(), jwtToken, 3, TimeUnit.HOURS);

            return TokenResponse.builder().successful(true).token(jwtToken).build();
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public TokenResponse login(LoginRequest loginRequest) {
        try {
            Auth auth = new Auth();
            String jwtToken = "";

            try {
                auth = authRepository.findByUsername(loginRequest.getUsername());
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }
            if (auth == null) {
                throw new CustomException("username not found, please register!");
            } else if (auth.getStatus().equals(AccountStatus.DISABLED)) {
                throw new CustomException("the account is deleted!");
            }

            if (passwordEncoder.matches(loginRequest.getPassword(), auth.getPassword())) {
                String redisJwt = redisTemplate.opsForValue().get("jwt::" + loginRequest.getUsername());
                if (redisJwt != null) {
                    redisTemplate.opsForValue().set("blackList::" + redisJwt, redisJwt, 3, TimeUnit.HOURS);
                }

                jwtToken = jwtUtil.generateToken(authConvert.authConvertToAuthResponse(auth));

                redisTemplate.opsForValue().set("jwt::" + loginRequest.getUsername(), jwtToken, 3, TimeUnit.HOURS);

                return TokenResponse.builder()
                        .successful(true)
                        .token(jwtToken)
                        .build();
            } else {
                throw new CustomException("wrong username or password!");
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
    @Cacheable(value="auth", key ="#username")
    public AuthResponse authentication(String username) {
        try {
            Auth auth = new Auth();

            try {
                auth = authRepository.findByUsername(username);
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }
            if (auth == null) {
                throw new CustomException("username not found, please register!");
            } else {
                if (auth.getStatus().equals(AccountStatus.DISABLED)) {
                    throw new CustomException("username not found!");
                }
            }

            return authConvert.authConvertToAuthResponse(auth);
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public void logout(String token) {
        try {
            redisTemplate.opsForValue().set("blackList::" + token, token, 3, TimeUnit.HOURS);
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public boolean checkBlackList(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey("blackList::" + token));
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public boolean disableUser(String username) {
        try {
            Auth auth = new Auth();

            try {
                auth = authRepository.findByUsername(username);
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }
            auth.setStatus(AccountStatus.DISABLED);

            try {
                authRepository.save(auth);
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            try {
                log.info("Send disableUserTopic");
                kafkaTemplate.send("disableUserTopic", username);
            } catch (Exception e) {
                log.error("Failed sending disableUserTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR!");
            }

            return true;
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

}
