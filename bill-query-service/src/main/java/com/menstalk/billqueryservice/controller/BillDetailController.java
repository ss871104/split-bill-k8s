package com.menstalk.billqueryservice.controller;

import com.menstalk.billqueryservice.dto.ErrorResponse;
import com.menstalk.billqueryservice.services.BillDetailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/billDetail")
@RequiredArgsConstructor
@Api(tags = "Bill Detail Query Api")
@Slf4j
public class BillDetailController {
    private final BillDetailService billDetailService;

    @GetMapping("/{billId}")
    @ApiOperation("(External) Show bill details info by billId")
    public ResponseEntity getBillDetailsByBillId(@PathVariable Long billId) {
        try {
            log.info("Received getBillDetailsByBillId request with billId: {}", billId);
            return new ResponseEntity<>(billDetailService.getAllByBillId(billId), HttpStatus.OK);
        } catch (Exception e) {
            log.error("GetBillDetailsByBillId failed for billId: {}, error: {}", billId, e.toString());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
