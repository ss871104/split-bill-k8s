package com.menstalk.billcommandservice.services;

import com.menstalk.billcommandservice.dto.BillPlacedRequest;
import com.menstalk.billcommandservice.dto.BillUpdateRequest;

public interface BillService {
    void addBillTransfer(BillPlacedRequest billPlacedRequest);
    void addBillAA(BillPlacedRequest billPlacedRequest);
    void addBillGoDutch(BillPlacedRequest billPlacedRequest);
    void updateBillTransfer(BillUpdateRequest billUpdateRequest);
    void updateBillAA(BillUpdateRequest billUpdateRequest);
    void updateBillGoDutch(BillUpdateRequest billUpdateRequest);
    void removeBill(Long billId);
}
