package com.menstalk.masterqueryservice.controller;

import com.menstalk.masterqueryservice.dto.ErrorResponse;
import com.menstalk.masterqueryservice.services.PartyService;
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
import java.util.List;

@RestController
@RequestMapping("/api/party")
@RequiredArgsConstructor
@Api(tags = "Party Query Api")
@Slf4j
public class PartyController {
    private final PartyService partyService;

    @GetMapping("/info")
    @ApiOperation("(External) Show partys info by username from token")
    public ResponseEntity getPartysByUsername(@RequestHeader("Authenticated-User") String username) {
        try {
            log.info("Received getPartysByUsername request with username: {}", username);
            return new ResponseEntity<>(partyService.getPartys(username), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("GetPartysByUsername failed with username: {}, error: {}", username, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{partyId}")
    @Retry(name = "default", fallbackMethod = "fallbackResponse")
    @ApiOperation("(Internal) Find party by partyId")
    ResponseEntity getPartyById(@PathVariable Long partyId) {
        log.info("Received getPartyById request with partyId: {}", partyId);
        return new ResponseEntity<>(partyService.getPartyById(partyId), HttpStatus.OK);
    }

    public ResponseEntity fallbackResponse(Exception e) {
        e.printStackTrace();
        log.error("GetPartyById failed, error: {}", e.toString());
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/admin/findAll/{pageNumber}")
    @ApiOperation("(Admin) Find all partys")
    public ResponseEntity findAll(@RequestHeader("Authenticated-User") String username, @PathVariable Integer pageNumber) {
        try {
            log.info("(Admin) Received findAllPartys request from username: {} with page {}", username, pageNumber);
            List<String> adminList = Files.readAllLines(Paths.get("/admin/admin.txt"));

            if (adminList.contains(username)) {
                return new ResponseEntity<>(partyService.getAllPartys(pageNumber), HttpStatus.OK);
            } else {
                log.error("(Admin) FindAllPartys failed for username: {}, unauthorized", username);
                return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("(Admin) FindAllPartys failed for username: {}, error: {}", username, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
