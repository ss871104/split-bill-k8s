package com.menstalk.authservice.controller;

import com.menstalk.authservice.dto.*;
import com.menstalk.authservice.handler.CustomException;
import com.menstalk.authservice.services.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Api(tags = "Auth Api")
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/api/auth/register")
    @ApiOperation("(External) Register by username, password, name")
    public ResponseEntity register(@RequestBody @Valid RegisterRequest registerRequest) {
        try {
            log.info("Received RegisterRequest, username: {}", registerRequest.getUsername());
            return ResponseEntity.ok(authService.register(registerRequest));
        } catch (CustomException e) {
            e.printStackTrace();
            log.error("Register failed, username: {}, error: {}", registerRequest.getUsername(), e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/auth/login")
    @ApiOperation("(External) Login by username and password")
    public ResponseEntity login(@RequestBody LoginRequest loginRequest) {
        try {
            log.info("Received LoginRequest, username: {}", loginRequest.getUsername());
            return ResponseEntity.ok(authService.login(loginRequest));
        } catch (CustomException e) {
            e.printStackTrace();
            log.error("Login failed, username: {}, error: {}", loginRequest.getUsername(), e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/auth/authentication")
    @ApiOperation("(Internal) Authentication for token")
    public ResponseEntity authentication(@RequestBody String username) {
        try {
            log.info("Authentication request from username: {}", username);
            return ResponseEntity.ok(authService.authentication(username));
        } catch (CustomException e) {
            e.printStackTrace();
            log.error("Authentication failed for username: {}", username);
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/api/auth/logout")
    @ApiOperation("(External) Logout, add current token to blacklist")
    public void logout(@RequestHeader(value = "Authorization") String token) {
        authService.logout(token.substring(7));
        log.info("Token blacklisted by logout, {}", token);
    }

    @PostMapping("/api/auth/checkBlackList")
    @ApiOperation("(Internal) Check whether token in blacklist")
    public ResponseEntity<Boolean> checkBlackList(@RequestBody String token) {
        return ResponseEntity.ok(authService.checkBlackList(token));
    }

    @DeleteMapping ("/api/user/disableUser")
    @ApiOperation("(External) Disable user")
    public ResponseEntity disableUser(@RequestHeader("Authenticated-User") String username) {
        try {
            log.info("Received user disable request from username: {}", username);
            boolean isDisable = authService.disableUser(username);
            return new ResponseEntity<>(isDisable, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("User disable failed for username: {}, error: {}", username, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}
