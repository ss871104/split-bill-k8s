package com.menstalk.masterqueryservice.services.impl;

import com.menstalk.masterqueryservice.dto.MemberResponse;
import com.menstalk.masterqueryservice.handler.CustomException;
import com.menstalk.masterqueryservice.mapper.MemberConvert;
import com.menstalk.masterqueryservice.models.Member;
import com.menstalk.masterqueryservice.repositories.MemberRepository;
import com.menstalk.masterqueryservice.services.MemberService;
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
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = "memberService")
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final MemberConvert memberConvert;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<MemberResponse> getMembersByPartyId(Long partyId) {
        try {
            List<Member> members = new ArrayList<>();
            List<MemberResponse> memberResponseList = new ArrayList<>();

            try {
                members = memberRepository.findMemberByPartyId(partyId);
                members.forEach(x -> {
                    if (Boolean.FALSE.equals(redisTemplate.hasKey("member::" + x.getMemberId()))) {
                        redisTemplate.opsForValue().set("member::" + x.getMemberId(), memberConvert.memberConvertToMemberResponse(x));
                        memberResponseList.add(memberConvert.memberConvertToMemberResponse(x));
                    } else {
                        memberResponseList.add((MemberResponse) redisTemplate.opsForValue().get("member::" + x.getMemberId()));
                    }
                });
            } catch (Exception e) {
                throw new CustomException("DB ERROR!");
            }
            if (members.size() == 0) {
                throw new CustomException("member not found!");
            }

            return memberResponseList;
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public List<MemberResponse> getAllMembers(Integer pageNumber) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, 20, Sort.by("memberId").ascending());
            Page<Member> page;

            try {
                page = memberRepository.findAll(pageable);
            } catch (Exception e) {
                throw new CustomException("DB ERROR!");
            }

            if (page.getNumberOfElements() == 0) {
                throw new CustomException("no data!");
            }

            return page.getContent().stream()
                    .map(memberConvert::memberConvertToMemberResponse)
                    .collect(Collectors.toList());
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public List<MemberResponse> getMembersForOwedWeeklyNotification() {
        try {
            List<Member> members = new ArrayList<>();

            try {
                members = memberRepository.findActiveMemberWithNegativeBalance();
            } catch (Exception e) {
                throw new CustomException("DB ERROR!");
            }

            return members.stream()
                    .map(memberConvert::memberConvertToMemberResponse)
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
