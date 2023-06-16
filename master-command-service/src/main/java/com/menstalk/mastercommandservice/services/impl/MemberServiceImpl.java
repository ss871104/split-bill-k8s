package com.menstalk.mastercommandservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstalk.mastercommandservice.dto.BalanceUpdateRequest;
import com.menstalk.mastercommandservice.dto.BillDetailType;
import com.menstalk.mastercommandservice.handler.CustomException;
import com.menstalk.mastercommandservice.kafka.event.NewMemberEvent;
import com.menstalk.mastercommandservice.models.Member;
import com.menstalk.mastercommandservice.models.Party;
import com.menstalk.mastercommandservice.models.Status;
import com.menstalk.mastercommandservice.models.User;
import com.menstalk.mastercommandservice.repositories.MemberRepository;
import com.menstalk.mastercommandservice.repositories.PartyRepository;
import com.menstalk.mastercommandservice.repositories.UserRepository;
import com.menstalk.mastercommandservice.services.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final PartyRepository partyRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void updateBalance(List<BalanceUpdateRequest> billRequests) {
        try {
            Set<Long> memberIds = new HashSet<>();
            List<Member> memberListOrigin = new ArrayList<>();
            Map<Long, Member> memberMap = new HashMap<>();
            List<Member> memberList = new ArrayList<>();

            memberIds = billRequests.stream()
                    .map(BalanceUpdateRequest::getMemberId)
                    .collect(Collectors.toSet());

            memberListOrigin = memberRepository.findAllById(memberIds);

            memberListOrigin.forEach(x -> memberMap.put(x.getMemberId(), x));

            billRequests.forEach(x -> {
                Member member = memberMap.get(x.getMemberId());
                Long newBalance = 0L;
                if (x.getBillDetailType() == BillDetailType.EXPENSE) {
                    newBalance = member.getBalance() - x.getAmount();

                } else {
                    newBalance = member.getBalance() + x.getAmount();
                }
                member.setBalance(newBalance);
                memberMap.put(x.getMemberId(), member);
            });
            memberList = new ArrayList<>(memberMap.values());
            try {
                memberList = memberRepository.saveAll(memberList);
                memberRepository.flush();
                memberList.forEach(x -> {
                    redisTemplate.delete("member::" + x.getMemberId());
                });
            } catch (Exception e) {
                e.printStackTrace();
                throw new CustomException("DB ERROR!");
            }

            String memberJson = objectMapper.writeValueAsString(memberList);

            try {
                log.info("Send updateBalanceTopic");
                kafkaTemplate.send("updateBalanceTopic", memberJson);
            } catch (Exception e) {
                log.error("Failed sending updateBalanceTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR");
            }
        } catch (ObjectOptimisticLockingFailureException oe) {
            log.error("ObjectOptimisticLockingFailureException from updateBalance, error: {}", oe.toString());
            throw new CustomException("Optimistic locking occur");
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    @CacheEvict(value = "member", key = "#memberId")
    public void disableMember(Long memberId) {
        try {
            Member member = new Member();
            List<Member> memberList = new ArrayList<>();
            Party party = new Party();

            member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException("member not exist!"));
            member.setMemberStatus(Status.DISABLED);

            memberList = memberRepository.findActiveMembersByPartyId(member.getPartyId());

            try {
                if (memberList == null || memberList.size() == 0) {
                    party = partyRepository.findById(member.getPartyId()).orElseThrow(() -> new CustomException("party not exist!"));
                    party.setPartyStatus(Status.DISABLED);
                }
                memberRepository.save(member);
                redisTemplate.delete("member::" + memberId);
                partyRepository.save(party);
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            String memberJson = objectMapper.writeValueAsString(member);

            try {
                log.info("Send disableMemberTopic");
                kafkaTemplate.send("disableMemberTopic", memberJson);
            } catch (Exception e) {
                log.error("Failed sending disableMemberTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR");
            }
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public void addMember(String username, String inviteId) {
        try {
            Member member = new Member();
            User user = new User();
            Long partyId = 0L;
            Integer partyIdInteger = 0;
            List<Member> members = new ArrayList<>();
            List<Long> userIds = new ArrayList<>();
            NewMemberEvent newMemberEvent = new NewMemberEvent();

            user = userRepository.findByUsername(username);
            if (user == null) {
                throw new CustomException("username not found");
            }

            if (Boolean.TRUE.equals(redisTemplate.hasKey("inviteId::" + inviteId))) {
                partyIdInteger = (Integer) redisTemplate.opsForValue().get("inviteId::" + inviteId);
                partyId = Long.valueOf(partyIdInteger);
            } else {
                throw new CustomException("invalid invite id");
            }

            member = memberRepository.findMemberByPartyIdAndUserId(partyId, user.getUserId());

            if (member == null) {
                member = Member.builder()
                        .partyId(partyId)
                        .userId(user.getUserId())
                        .memberNickname(user.getName())
                        .balance(0L)
                        .createTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                        .memberStatus(Status.ACTIVE)
                        .build();
            } else {
                if (member.getMemberStatus().equals(Status.DISABLED)) {
                    member.setMemberStatus(Status.ACTIVE);
                    member.setMemberNickname(user.getName());
                    member.setBalance(0L);
                    member.setCreateTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
                } else {
                    throw new CustomException("user is already in party");
                }
            }

            try {
                member = memberRepository.save(member);
                memberRepository.flush();
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            try {
                members = memberRepository.findActiveMembersByPartyId(partyId);
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }
            userIds = members.stream()
                    .map(Member::getUserId)
                    .collect(Collectors.toList());

            String memberJson = objectMapper.writeValueAsString(member);
            String userIdsJson = objectMapper.writeValueAsString(userIds);

            newMemberEvent.setMemberName(user.getName());
            newMemberEvent.setPartyId(partyId);
            newMemberEvent.setUserIds(userIdsJson);

            String newMemberEventString = objectMapper.writeValueAsString(newMemberEvent);

            try {
                log.info("Send addMemberTopic");
                kafkaTemplate.send("addMemberTopic", memberJson);
            } catch (Exception e) {
                log.error("Failed sending addMemberTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR");
            }
            try {
                log.info("Send newMemberTopic");
                kafkaTemplate.send("newMemberTopic", newMemberEventString);
            } catch (Exception e) {
                log.error("Failed sending newMemberTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR");
            }
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public String generateInviteURL(String username, Long partyId) {
        try {
            Long userId = 0L;
            Member member = new Member();
            String inviteId = "";

            try {
                userId = userRepository.findByUsername(username).getUserId();
                member = memberRepository.findActiveMemberByPartyIdAndUserId(partyId, userId);
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }
            if (member == null) {
                throw new CustomException("Not allow to generate invite url");
            }
            inviteId = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set("inviteId::" + inviteId, partyId, 1, TimeUnit.HOURS);

            return inviteId;
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }
}
