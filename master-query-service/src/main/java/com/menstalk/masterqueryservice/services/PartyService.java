package com.menstalk.masterqueryservice.services;


import com.menstalk.masterqueryservice.dto.PartyResponse;

import java.util.List;

public interface PartyService {
    List<PartyResponse> getPartys(String username);
    PartyResponse getPartyById(Long partyId);
    List<PartyResponse> getAllPartys(Integer pageNumber);
}
