package com.menstalk.masterqueryservice.controller;

import com.menstalk.masterqueryservice.dto.ErrorResponse;
import com.menstalk.masterqueryservice.dto.MemberResponse;
import com.menstalk.masterqueryservice.services.MemberService;
import io.github.resilience4j.retry.annotation.Retry;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Api(tags = "Member Query Api")
@Slf4j
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/partyId/{partyId}")
    @Retry(name = "default", fallbackMethod = "fallbackResponse")
    @ApiOperation("(External) Show members info by partyId")
    public ResponseEntity getMembersByPartyId(@PathVariable Long partyId) {
        log.info("Received getMembersByPartyId request with partyId: {}", partyId);
        return new ResponseEntity<>(memberService.getMembersByPartyId(partyId), HttpStatus.OK);
    }

    public ResponseEntity fallbackResponse(Exception e) {
        e.printStackTrace();
        log.error("GetMembersByPartyId failed error: {}", e.toString());
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/owedWeekly")
    @Retry(name = "default", fallbackMethod = "fallbackResponse2")
    @ApiOperation("(Internal) Show members info by partyId for weekly owed notification")
    ResponseEntity getMembersForOwedWeeklyNotification() {
        log.info("Received getMembersForOwedWeeklyNotification request for batch by {}", LocalDateTime.now().truncatedTo(TimeUnit.SECONDS.toChronoUnit()));
        return new ResponseEntity<>(memberService.getMembersForOwedWeeklyNotification(), HttpStatus.OK);
    }

    public ResponseEntity fallbackResponse2(Exception e) {
        e.printStackTrace();
        log.error("GetMembersForOwedWeeklyNotification failed for batch by {}, error: {}", LocalDateTime.now().truncatedTo(TimeUnit.SECONDS.toChronoUnit()), e.toString());
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/admin/findAll/{pageNumber}")
    @ApiOperation("(Admin) Find all members")
    public ResponseEntity findAll(@RequestHeader("Authenticated-User") String username, @PathVariable Integer pageNumber) {
        try {
            log.info("(Admin) Received findAllMembers request from username: {} with page {}", username, pageNumber);
            List<String> adminList = Files.readAllLines(Paths.get("/admin/admin.txt"));

            if (adminList.contains(username)) {
                return new ResponseEntity<>(memberService.getAllMembers(pageNumber), HttpStatus.OK);
            } else {
                log.error("(Admin) FindAllMembers failed for username: {}, unauthorized", username);
                return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            log.error("(Admin) FindAllMembers failed for username: {}, error: {}", username, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
