package com.menstalk.batchservice.batch;

import com.menstalk.batchservice.dto.MemberResponse;
import com.menstalk.batchservice.proxy.MasterQueryProxy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class OwedWeeklyNotificationReader implements ItemReader<Map<Long, List<MemberResponse>>> {
    private final MasterQueryProxy masterQueryProxy;
    private boolean readAlready = false;

    @Override
    public Map<Long, List<MemberResponse>> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        log.info("Weekly Owed Notification batch start!");

        if (readAlready) {
            return null;
        }

        List<MemberResponse> memberResponses = new ArrayList<>();

        memberResponses = masterQueryProxy.getMembersForOwedWeeklyNotification().getBody();

        readAlready = true;

        return memberResponses.stream()
                .collect(Collectors.groupingBy(MemberResponse::getPartyId, Collectors.toList()));

    }
}
