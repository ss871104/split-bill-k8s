package com.menstalk.mastercommandservice.services;

public interface PartyService {
	void addParty(String partyName, String username);

	void disableParty(Long partyId);

	void updatePartyName(Long partyId, String partyName);

}
