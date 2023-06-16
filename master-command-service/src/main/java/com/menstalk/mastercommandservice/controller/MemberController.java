package com.menstalk.mastercommandservice.controller;

import com.menstalk.mastercommandservice.dto.BalanceUpdateRequest;
import com.menstalk.mastercommandservice.dto.ErrorResponse;
import com.menstalk.mastercommandservice.models.Member;
import com.menstalk.mastercommandservice.services.MemberService;
import io.github.resilience4j.retry.annotation.Retry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Api(tags = "Member Command Api")
@Slf4j
public class MemberController {
    private final MemberService memberService;

    @PutMapping("/updateBalance")
    @Retry(name = "default", fallbackMethod = "fallbackResponse")
    @ApiOperation("(Internal) Update member balance")
    public ResponseEntity updateBalance(@RequestBody List<BalanceUpdateRequest> billRequests) {
        log.info("Received updateBalance request from bill-service");
        memberService.updateBalance(billRequests);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    public ResponseEntity fallbackResponse(Exception e) {
        e.printStackTrace();
        log.error("UpdateBalance failed, error: {}", e.toString());
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/disableMember/{memberId}")
    @ApiOperation("(External) Disable member")
    public ResponseEntity disableMember(@PathVariable Long memberId) {
        try {
            log.info("Received disableMember request for memberId: {}", memberId);
            memberService.disableMember(memberId);
            return new ResponseEntity<>(true, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("DisableMember failed for memberId: {}, error: {}", memberId, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/addMember")
    @ApiOperation("(External) Add member")
    public ResponseEntity addMember(@RequestHeader("Authenticated-User") String username, @RequestParam("inviteId") String inviteId) {
        try {
            log.info("Received addMember request for username: {} with inviteId: {}", username, inviteId);
            memberService.addMember(username, inviteId);
            return new ResponseEntity<>(true, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("AddMember failed for username: {} with inviteId: {}, error: {}", username, inviteId, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/generateInviteURL/{partyId}")
    @ApiOperation("(External) Generate party invite url")
    public ResponseEntity generateInviteURL(@RequestHeader("Authenticated-User") String username, @PathVariable Long partyId) {
        try {
            log.info("Received generateInviteURL for partyId: {} from username: {}", partyId, username);
            String inviteId = "";

            inviteId = memberService.generateInviteURL(username, partyId);

            return new ResponseEntity<>(inviteId, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("GenerateInviteURL failed for partyId: {} from username: {}, error: {}", partyId, username, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
