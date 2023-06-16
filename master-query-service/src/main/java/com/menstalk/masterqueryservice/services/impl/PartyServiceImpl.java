package com.menstalk.masterqueryservice.services.impl;

import com.menstalk.masterqueryservice.dto.PartyResponse;
import com.menstalk.masterqueryservice.handler.CustomException;
import com.menstalk.masterqueryservice.mapper.PartyConvert;
import com.menstalk.masterqueryservice.models.Member;
import com.menstalk.masterqueryservice.models.Party;
import com.menstalk.masterqueryservice.models.User;
import com.menstalk.masterqueryservice.repositories.MemberRepository;
import com.menstalk.masterqueryservice.repositories.PartyRepository;
import com.menstalk.masterqueryservice.repositories.UserRepository;
import com.menstalk.masterqueryservice.services.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = "partyService")
public class PartyServiceImpl implements PartyService {
    private final PartyRepository partyRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PartyConvert partyConvert;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<PartyResponse> getPartys(String username) {
        try {
            List<Party> partys = new ArrayList<>();
            User user = new User();
            List<Member> members = new ArrayList<>();
            List<Long> partyIds = new ArrayList<>();
            List<PartyResponse> partyResponses = new ArrayList<>();

            try {
                user = userRepository.findByUsername(username);
            } catch (Exception e) {
                throw new CustomException("DB ERROR!");
            }

            try {
                if (user != null) {
                    members = memberRepository.findActiveMemberByUserId(user.getUserId());
                }
            } catch (Exception e) {
                throw new CustomException("DB ERROR!");
            }
            if (members == null || members.size() == 0) {
                return partyResponses;
            }
            partyIds = members.stream()
                    .map(Member::getPartyId)
                    .collect(Collectors.toList());

            try {
                partys = partyRepository.findAllById(partyIds);
            } catch (Exception e) {
                throw new CustomException("DB ERROR!");
            }
            if (partys.size() == 0) {
                throw new CustomException("party not found!");
            }

            partys.forEach(x -> {
                PartyResponse partyResponse = partyConvert.partyConvertToPartyResponse(x);
                partyResponses.add(partyResponse);
                redisTemplate.opsForValue().set("party::" + x.getPartyId(), partyResponse, 1, TimeUnit.HOURS);
            });

            return partyResponses;
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public PartyResponse getPartyById(Long partyId) {
        try {
            Party party = new Party();

            if (Boolean.TRUE.equals(redisTemplate.hasKey("party::" + partyId))) {
                return (PartyResponse) redisTemplate.opsForValue().get("party::" + partyId);
            }

            try {
                party = partyRepository.findById(partyId).orElseThrow(() -> new CustomException("party not found"));
            } catch (Exception e) {
                throw new CustomException("DB ERROR!");
            }

            return partyConvert.partyConvertToPartyResponse(party);
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public List<PartyResponse> getAllPartys(Integer pageNumber) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, 20, Sort.by("partyId").ascending());
            Page<Party> page;

            try {
                page = partyRepository.findAll(pageable);
            } catch (Exception e) {
                throw new CustomException("DB ERROR!");
            }

            if (page.getNumberOfElements() == 0) {
                throw new CustomException("no data!");
            }

            return page.getContent().stream()
                    .map(partyConvert::partyConvertToPartyResponse)
                    .collect(Collectors.toList());
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }
}
