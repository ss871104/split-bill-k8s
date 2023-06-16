package com.menstalk.mastercommandservice.controller;

import com.menstalk.mastercommandservice.dto.ErrorResponse;
import com.menstalk.mastercommandservice.models.User;
import com.menstalk.mastercommandservice.services.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Api(tags = "User Command Api")
@Slf4j
public class UserController {
    private final UserService userService;

    @PutMapping("/updateName")
    @ApiOperation("(External) Update user name")
    public ResponseEntity updateName(@RequestHeader("Authenticated-User") String username, @RequestBody User user) {
        try {
            log.info("Received updateName request from username: {}", username);
            userService.updateName(username, user.getName());
            return new ResponseEntity<>(true, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("UpdateName failed for username: {}, error: {}", username, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping ("/disableUser")
    @ApiOperation("(External) Disable user")
    public ResponseEntity disableUser(@RequestHeader("Authenticated-User") String username) {
        try {
            log.info("Received disableUser request from username: {}", username);
            userService.disableUser(username);
            return new ResponseEntity<>(true, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("DisableUser failed for username: {}, error: {}", username, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}
