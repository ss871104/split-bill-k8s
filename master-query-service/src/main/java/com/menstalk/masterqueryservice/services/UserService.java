package com.menstalk.masterqueryservice.services;

import com.menstalk.masterqueryservice.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse getByUsername(String username);
    List<UserResponse> getAllUsers(Integer pageNumber);
}
