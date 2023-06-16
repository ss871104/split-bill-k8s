package com.menstalk.masterqueryservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstalk.masterqueryservice.kafka.event.AddPartyEvent;
import com.menstalk.masterqueryservice.kafka.event.DisablePartyEvent;
import com.menstalk.masterqueryservice.models.Member;
import com.menstalk.masterqueryservice.models.Party;
import com.menstalk.masterqueryservice.repositories.MemberRepository;
import com.menstalk.masterqueryservice.repositories.PartyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PartyCqrs {
    private final PartyRepository partyRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "addPartyTopic")
    public void addPartyEvent(String addPartyEventString) throws JsonProcessingException {
        AddPartyEvent addPartyEvent = objectMapper.readValue(addPartyEventString, AddPartyEvent.class);

        Party party = objectMapper.readValue(addPartyEvent.getParty(), Party.class);
        Member member = objectMapper.readValue(addPartyEvent.getMember(), Member.class);
        log.info("Received party cqrs from addPartyEvent, party name: {}, userId: {}", party.getPartyName(), member.getUserId());
        try {
            partyRepository.save(party);
            memberRepository.save(member);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Party cqrs from addPartyEvent failed, party name: {} add failed by userId: {}, error: {}", party.getPartyName(), member.getUserId(), e.toString());
        }

    }

    @KafkaListener(topics = "disablePartyTopic")
    public void disablePartyEvent(String disablePartyEventString) throws JsonProcessingException {
        DisablePartyEvent disablePartyEvent = objectMapper.readValue(disablePartyEventString, DisablePartyEvent.class);

        Party party = objectMapper.readValue(disablePartyEvent.getParty(), Party.class);
        List<Member> members = objectMapper.readValue(disablePartyEvent.getMembers(), new TypeReference<List<Member>>(){});
        log.info("Received party cqrs from disablePartyEvent, partyId: {}", party.getPartyId());
        try {
            partyRepository.save(party);
            memberRepository.saveAll(members);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Party cqrs from disablePartyEvent failed, partyId: {} disable failed, error: {}", party.getPartyId(), e.toString());
        }

    }

    @KafkaListener(topics = "updatePartyNameTopic")
    public void updatePartyNameEvent(String partyMessage) throws JsonProcessingException {
        Party party = objectMapper.readValue(partyMessage, Party.class);
        log.info("Received party cqrs from updatePartyNameEvent, partyId: {}", party.getPartyId());
        try {
            partyRepository.save(party);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Party cqrs from updatePartyNameEvent failed, partyId: {} name update failed, error: {}", party.getPartyId(), e.toString());
        }

    }
}
