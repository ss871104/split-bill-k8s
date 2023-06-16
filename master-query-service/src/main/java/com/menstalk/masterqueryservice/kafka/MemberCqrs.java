package com.menstalk.masterqueryservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstalk.masterqueryservice.models.Member;
import com.menstalk.masterqueryservice.repositories.MemberRepository;
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
public class MemberCqrs {
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "updateBalanceTopic")
    public void updateBalanceEvent(String memberMessages) throws JsonProcessingException {
        List<Member> members = objectMapper.readValue(memberMessages, new TypeReference<List<Member>>(){});
        log.info("Received member cqrs from updateBalanceEvent, partyId: {}", members.get(0).getPartyId());
        try {
            memberRepository.saveAll(members);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Member cqrs from updateBalanceEvent failed, balance update failed in partyId: {}, error: {}", members.get(0).getPartyId(), e.toString());
        }

    }

    @KafkaListener(topics = "disableMemberTopic")
    public void disableMemberEvent(String memberMessage) throws JsonProcessingException {
        Member member = objectMapper.readValue(memberMessage, Member.class);
        log.info("Received member cqrs from disableMemberEvent, memberId: {}", member.getMemberId());
        try {
            memberRepository.save(member);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Member cqrs from disableMemberEvent failed, memberId: {} disable failed, error: {}", member.getMemberId(), e.toString());
        }

    }

    @KafkaListener(topics = "addMemberTopic")
    public void addMemberEvent(String memberMessage) throws JsonProcessingException {
        Member member = objectMapper.readValue(memberMessage, Member.class);
        log.info("Received member cqrs from addMemberEvent, userId: {}, partyId: {}", member.getUserId(), member.getPartyId());
        try {
            memberRepository.save(member);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Member cqrs from addMemberEvent failed, userId: {} add failed to partyId: {}, error: {}", member.getUserId(), member.getPartyId(), e.toString());
        }

    }
}
