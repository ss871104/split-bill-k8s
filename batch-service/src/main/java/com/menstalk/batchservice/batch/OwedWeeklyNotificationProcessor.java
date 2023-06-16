package com.menstalk.batchservice.batch;

import com.menstalk.batchservice.dto.MemberResponse;
import com.menstalk.batchservice.models.Notification;
import com.menstalk.batchservice.models.NotificationKey;
import com.menstalk.batchservice.models.NotificationType;
import com.menstalk.batchservice.proxy.MasterQueryProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OwedWeeklyNotificationProcessor implements ItemProcessor<Map<Long, List<MemberResponse>>, List<Notification>> {
    private final MasterQueryProxy masterQueryProxy;

    @Override
    public List<Notification> process(Map<Long, List<MemberResponse>> longMemberResponseMap) throws Exception {
        List<Notification> notifications = new ArrayList<>();

        longMemberResponseMap.forEach((key, value) -> {
            List<Long> userIds = new ArrayList<>();
            StringBuilder memberFormatted = new StringBuilder();
            List<MemberResponse> memberResponseList = new ArrayList<>();
            List<Notification> notificationList = new ArrayList<>();

            memberResponseList = masterQueryProxy.getMembersByPartyId(key).getBody();

            for (MemberResponse memberResponse : value) {
                if (memberFormatted.length() == 0) {
                    memberFormatted = new StringBuilder(memberResponse.getMemberNickname());
                } else {
                    memberFormatted.append(" & ").append(memberResponse.getMemberNickname());
                }
            }
            if (value.size() > 1) {
                memberFormatted.append(", ").append(String.valueOf(value.size())).append("位");
            } else {
                memberFormatted.append(", ");
            }

            final var finalMemberFormatted = memberFormatted;

            userIds = memberResponseList.stream()
                    .map(MemberResponse::getUserId)
                    .collect(Collectors.toList());

            notificationList = userIds.stream()
                    .map(userId -> Notification.builder()
                            .notificationKey(new NotificationKey(userId, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), UUID.randomUUID()))
                            .title(NotificationType.OWED_WEEKLY.getTitle())
                            .content(NotificationType.OWED_WEEKLY.getContent().formatted(finalMemberFormatted))
                            .isRead(false)
                            .build())
                    .collect(Collectors.toList());

            notifications.addAll(notificationList);
        });

        return notifications;
    }
}
