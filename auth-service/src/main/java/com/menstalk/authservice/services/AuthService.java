package com.menstalk.authservice.services;


import com.menstalk.authservice.dto.AuthResponse;
import com.menstalk.authservice.dto.LoginRequest;
import com.menstalk.authservice.dto.RegisterRequest;
import com.menstalk.authservice.dto.TokenResponse;

public interface AuthService {
    TokenResponse register(RegisterRequest registerRequest);
    TokenResponse login(LoginRequest loginRequest);
    AuthResponse authentication(String username);
    void logout(String token);
    boolean checkBlackList(String token);
    boolean disableUser(String username);
}
