package com.menstalk.billqueryservice.controller;

import com.menstalk.billqueryservice.dto.ErrorResponse;
import com.menstalk.billqueryservice.services.BillService;
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
@RequestMapping("/api/bill")
@RequiredArgsConstructor
@Api(tags = "Bill Query Api")
@Slf4j
public class BillController {
    private final BillService billService;

    @GetMapping("/{partyId}/{pageNumber}")
    @ApiOperation("(External) Show bills info by partyId")
    public ResponseEntity getBillsByPartyId(@PathVariable Long partyId, @PathVariable Integer pageNumber) {
        try {
            log.info("Received getBillsByPartyId request from partyId: {} with page {}", partyId, pageNumber);
            return new ResponseEntity<>(billService.getAllByPartyId(partyId, pageNumber), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("GetBillsByPartyId failed for partyId: {}, error: {}", partyId, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/admin/findAll/{pageNumber}")
    @ApiOperation("(Admin) Find all bills")
    public ResponseEntity findAll(@RequestHeader("Authenticated-User") String username, @PathVariable Integer pageNumber) {
        try {
            log.info("(Admin) Received findAllBills request from username: {} with page {}", username, pageNumber);
            List<String> adminList = Files.readAllLines(Paths.get("/admin/admin.txt"));

            if (adminList.contains(username)) {
                return new ResponseEntity<>(billService.getAllBills(pageNumber), HttpStatus.OK);
            } else {
                log.error("(Admin) FindAllBills failed for username: {}, unauthorized", username);
                return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("(Admin) FindAllBills failed for username: {}, error: {}", username, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
