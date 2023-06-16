package com.menstalk.billcommandservice.controller;

import com.menstalk.billcommandservice.dto.BillPlacedRequest;
import com.menstalk.billcommandservice.dto.BillUpdateRequest;
import com.menstalk.billcommandservice.dto.ErrorResponse;
import com.menstalk.billcommandservice.models.BillType;
import com.menstalk.billcommandservice.services.BillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bill")
@RequiredArgsConstructor
@Api(tags = "Bill Command Api")
@Slf4j
public class BillController {
    private final BillService billService;

    @PostMapping("/addBill")
    @ApiOperation("(External) Add Bill")
    public ResponseEntity addBill(@RequestBody BillPlacedRequest billPlacedRequest) {
        try {
            if (billPlacedRequest.getBillType() == BillType.TRANSFER) {
                log.info("Received bill request for TRANSFER from partyId: {}", billPlacedRequest.getPartyId());
                billService.addBillTransfer(billPlacedRequest);
            } else if (billPlacedRequest.getBillType() == BillType.AA) {
                log.info("Received bill request for AA from partyId: {}", billPlacedRequest.getPartyId());
                billService.addBillAA(billPlacedRequest);
            } else if (billPlacedRequest.getBillType() == BillType.GO_DUTCH) {
                log.info("Received bill request for GO DUTCH from partyId: {}", billPlacedRequest.getPartyId());
                billService.addBillGoDutch(billPlacedRequest);
            } else {
                log.error("Received bill request for UNKNOWN TYPE from partyId: {}", billPlacedRequest.getPartyId());
                return new ResponseEntity<>("unknown bill type", HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Bill request failed for partyId: {}, error: {}", billPlacedRequest.getPartyId(), e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/updateBill")
    @ApiOperation("(External) Update Bill")
    public ResponseEntity updateBill(@RequestBody BillUpdateRequest billUpdateRequest) {
        try {
            if (billUpdateRequest.getBillType() == BillType.TRANSFER) {
                log.info("Received bill update request to TRANSFER for billId: {}", billUpdateRequest.getBillId());
                billService.updateBillTransfer(billUpdateRequest);
            } else if (billUpdateRequest.getBillType() == BillType.AA) {
                log.info("Received bill update request to AA for billId: {}", billUpdateRequest.getBillId());
                billService.updateBillAA(billUpdateRequest);
            } else if (billUpdateRequest.getBillType() == BillType.GO_DUTCH) {
                log.info("Received bill update request to GO DUTCH for billId: {}", billUpdateRequest.getBillId());
                billService.updateBillGoDutch(billUpdateRequest);
            } else {
                log.error("Received bill update request to UNKNOWN TYPE for billId: {}", billUpdateRequest.getBillId());
                return new ResponseEntity<>("unknown bill type", HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(true, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Bill update request failed for billId: {}, error: {}", billUpdateRequest.getBillId(), e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/removeBill/{billId}")
    @ApiOperation("(External) Remove Bill")
    public ResponseEntity removeBill(@PathVariable Long billId) {
        try {
            log.info("Received bill remove request for billId: {}", billId);
            billService.removeBill(billId);
            return new ResponseEntity<>(true, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Bill remove request failed for billId: {}", billId);
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}
