package com.menstalk.mastercommandservice.controller;

import com.menstalk.mastercommandservice.dto.ErrorResponse;
import com.menstalk.mastercommandservice.models.Party;
import com.menstalk.mastercommandservice.services.PartyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/party")
@RequiredArgsConstructor
@Api(tags = "Party Command Api")
@Slf4j
public class PartyController {
    private final PartyService partyService;

    @PostMapping("/addParty")
    @ApiOperation("(External) Add party")
    public ResponseEntity addParty(@RequestHeader("Authenticated-User") String username, @RequestBody Party party) {
        try {
            log.info("Received addParty request from username: {}", username);
            partyService.addParty(party.getPartyName(), username);
            return new ResponseEntity<>(true, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("AddParty failed for username: {}, error: {}", username, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/disableParty/{partyId}")
    @ApiOperation("(External) Disable party")
    public ResponseEntity disableParty(@PathVariable Long partyId) {
        try {
            log.info("Received disableParty request for partyId: {}", partyId);
            partyService.disableParty(partyId);
            return new ResponseEntity<>(true, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("DisableParty failed for partyId: {}, error: {}", partyId, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/updatePartyName")
    @ApiOperation("(External) Update party name")
    public ResponseEntity updatePartyName(@RequestBody Party party) {
        try {
            log.info("Received updatePartyName request for partyId: {}", party.getPartyId());
            partyService.updatePartyName(party.getPartyId(), party.getPartyName());
            return new ResponseEntity<>(true, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("UpdatePartyName failed for partyId: {}, error: {}", party.getPartyId(), e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}
