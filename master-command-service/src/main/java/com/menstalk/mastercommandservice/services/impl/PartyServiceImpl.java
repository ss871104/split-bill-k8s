package com.menstalk.mastercommandservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstalk.mastercommandservice.handler.CustomException;
import com.menstalk.mastercommandservice.kafka.event.AddPartyEvent;
import com.menstalk.mastercommandservice.kafka.event.DisablePartyEvent;
import com.menstalk.mastercommandservice.models.Member;
import com.menstalk.mastercommandservice.models.Party;
import com.menstalk.mastercommandservice.models.Status;
import com.menstalk.mastercommandservice.models.User;
import com.menstalk.mastercommandservice.repositories.MemberRepository;
import com.menstalk.mastercommandservice.repositories.PartyRepository;
import com.menstalk.mastercommandservice.repositories.UserRepository;
import com.menstalk.mastercommandservice.services.PartyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PartyServiceImpl implements PartyService {

	private final PartyRepository partyRepository;
	private final MemberRepository memberRepository;
	private final UserRepository userRepository;
	private final RedisTemplate<String, Object> redisTemplate;
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	@Override
	public void addParty(String partyName, String username) {
		try {
			User user = new User();
			Member member = new Member();
			Party party = new Party();
			AddPartyEvent addPartyEvent = new AddPartyEvent();

			try {
				user = userRepository.findByUsername(username);
			} catch (Exception e) {
				e.printStackTrace();
				throw new CustomException("DB ERROR");
			}
			if (user == null) {
				throw new CustomException("username not found");
			}

			try {
				party = Party.builder()
							.partyName(partyName)
							.createTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
							.partyStatus(Status.ACTIVE)
							.build();
				party = partyRepository.save(party);
				partyRepository.flush();
			} catch (Exception e) {
				throw new CustomException("DB ERROR");
			}
			Long partyId = party.getPartyId();
			member = Member.builder()
						.memberNickname(user.getName())
						.userId(user.getUserId())
						.partyId(partyId)
						.balance(0L)
						.createTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
						.memberStatus(Status.ACTIVE).build();
			try {
				member = memberRepository.save(member);
				memberRepository.flush();
			} catch (Exception e) {
				throw new CustomException("DB ERROR");
			}

			String partyJson = objectMapper.writeValueAsString(party);
			String memberJson = objectMapper.writeValueAsString(member);

			addPartyEvent = AddPartyEvent.builder()
					.party(partyJson)
					.member(memberJson)
					.build();

			String addPartyEventString = objectMapper.writeValueAsString(addPartyEvent);

			try {
				log.info("Send addPartyTopic");
				kafkaTemplate.send("addPartyTopic", addPartyEventString);
			} catch (Exception e) {
				log.error("Failed sending addPartyTopic, error: {}", e.toString());
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
	@CacheEvict(value = "party", key = "#partyId")
	public void disableParty(Long partyId) {
		try {
			Party party = new Party();
			List<Member> members = new ArrayList<>();
			DisablePartyEvent disablePartyEvent = new DisablePartyEvent();

			party = partyRepository.findById(partyId).orElseThrow(() -> new CustomException("party not exist!"));
			members = memberRepository.findActiveMembersByPartyId(partyId);

			members.forEach(x -> {
				x.setMemberStatus(Status.DISABLED);
				redisTemplate.delete("member::" + x.getMemberId());
			});
			party.setPartyStatus(Status.DISABLED);
			try {
				memberRepository.saveAll(members);
				partyRepository.save(party);
			} catch (Exception e) {
				throw new CustomException("DB ERROR");
			}

			String partyJson = objectMapper.writeValueAsString(party);
			String membersJson = objectMapper.writeValueAsString(members);

			disablePartyEvent = DisablePartyEvent.builder()
					.party(partyJson)
					.members(membersJson)
					.build();

			String disablePartyEventString = objectMapper.writeValueAsString(disablePartyEvent);

			try {
				log.info("Send disablePartyTopic");
				kafkaTemplate.send("disablePartyTopic", disablePartyEventString);
			} catch (Exception e) {
				log.error("Failed sending disablePartyTopic, error: {}", e.toString());
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
	@CacheEvict(value = "party", key = "#partyId")
	public void updatePartyName(Long partyId, String partyName) {
		try {
			Party party = new Party();

			party = partyRepository.findById(partyId).orElseThrow(() -> new CustomException("party not exist!"));
			party.setPartyName(partyName);
			try {
				partyRepository.save(party);
			} catch (Exception e) {
				throw new CustomException("DB ERROR");
			}

			String partyJson = objectMapper.writeValueAsString(party);

			try {
				log.info("Send updatePartyNameTopic");
				kafkaTemplate.send("updatePartyNameTopic", partyJson);
			} catch (Exception e) {
				log.error("Failed sending updatePartyNameTopic, error: {}", e.toString());
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

}
