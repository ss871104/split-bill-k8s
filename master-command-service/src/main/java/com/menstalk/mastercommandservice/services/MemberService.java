package com.menstalk.mastercommandservice.services;

import com.menstalk.mastercommandservice.dto.BalanceUpdateRequest;

import java.util.List;

public interface MemberService {
    void updateBalance(List<BalanceUpdateRequest> billRequests);

    void disableMember(Long memberId);

    void addMember(String username, String inviteId);

    String generateInviteURL(String username, Long partyId);
}
