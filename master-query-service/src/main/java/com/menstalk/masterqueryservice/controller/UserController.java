package com.menstalk.masterqueryservice.controller;

import com.menstalk.masterqueryservice.dto.ErrorResponse;
import com.menstalk.masterqueryservice.services.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Api(tags = "User Query Api")
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/info")
    @ApiOperation("(External) Show user info by username from token")
    public ResponseEntity getUserByUsername(@RequestHeader("Authenticated-User") String username) {
        try {
            log.info("Received getUserByUsername request from username: {}", username);
            return new ResponseEntity<>(userService.getByUsername(username), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("GetUserByUsername failed for username: {}, error: {}", username, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/admin/findAll/{pageNumber}")
    @ApiOperation("(Admin) Find all users")
    public ResponseEntity findAll(@RequestHeader("Authenticated-User") String username, @PathVariable Integer pageNumber) {
        try {
            log.info("(Admin) Received findAllUsers request from username: {} with page {}", username, pageNumber);
            List<String> adminList = Files.readAllLines(Paths.get("/admin/admin.txt"));

            if (adminList.contains(username)) {
                return new ResponseEntity<>(userService.getAllUsers(pageNumber), HttpStatus.OK);
            } else {
                log.error("(Admin) FindAllUsers failed for username: {}, unauthorized", username);
                return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("(Admin) FindAllUsers failed for username: {}, error: {}", username, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}
