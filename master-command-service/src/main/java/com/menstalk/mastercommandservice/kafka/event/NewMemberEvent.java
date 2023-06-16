package com.menstalk.mastercommandservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewMemberEvent {
    private String userIds;
    private Long partyId;
    private String memberName;
}
