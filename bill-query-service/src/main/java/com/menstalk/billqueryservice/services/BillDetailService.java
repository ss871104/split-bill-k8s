package com.menstalk.billqueryservice.services;


import com.menstalk.billqueryservice.dto.BillDetailResponse;

import java.util.List;

public interface BillDetailService {
    List<BillDetailResponse> getAllByBillId(Long billId);
}
