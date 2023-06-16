package com.menstalk.masterqueryservice.services.impl;

import com.menstalk.masterqueryservice.dto.UserResponse;
import com.menstalk.masterqueryservice.handler.CustomException;
import com.menstalk.masterqueryservice.mapper.UserConvert;
import com.menstalk.masterqueryservice.models.User;
import com.menstalk.masterqueryservice.repositories.UserRepository;
import com.menstalk.masterqueryservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = "userService")
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserConvert userConvert;

    @Override
    @Cacheable(value="user", key ="#username")
    public UserResponse getByUsername(String username) {
        try {
            User user = new User();

            try {
                user = userRepository.findByUsername(username);
            } catch (Exception e) {
                throw new CustomException("DB ERROR!");
            }
            if (user == null) {
                throw new CustomException("user not found!");
            }

            return userConvert.userConvertToUserResponse(user);
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }

    }

    @Override
    public List<UserResponse> getAllUsers(Integer pageNumber) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, 20, Sort.by("userId").ascending());
            Page<User> page;

            try {
                page = userRepository.findAll(pageable);
            } catch (Exception e) {
                throw new CustomException("DB ERROR!");
            }

            if (page.getNumberOfElements() == 0) {
                throw new CustomException("no data!");
            }

            return page.getContent().stream()
                    .map(userConvert::userConvertToUserResponse)
                    .collect(Collectors.toList());
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

}
