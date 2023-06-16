package com.menstalk.masterqueryservice.services;

import com.menstalk.masterqueryservice.dto.MemberResponse;

import java.util.List;

public interface MemberService {
    List<MemberResponse> getMembersByPartyId(Long partyId);

    List<MemberResponse> getAllMembers(Integer pageNumber);

    List<MemberResponse> getMembersForOwedWeeklyNotification();
}
