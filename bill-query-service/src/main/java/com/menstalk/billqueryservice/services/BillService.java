package com.menstalk.billqueryservice.services;


import com.menstalk.billqueryservice.dto.BillResponse;

import java.util.List;

public interface BillService {
    List<BillResponse> getAllByPartyId(Long partyId, Integer pageNumber);
    List<BillResponse> getAllBills(Integer pageNumber);
}
