package com.menstalk.masterqueryservice.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Table(name = "\"member\"")
public class Member {
    @Id
    @Column(name = "member_id")
    private Long memberId;
    @Column(name = "party_id")
    private Long partyId;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "balance")
    private Long balance;
    @Column(name = "member_nickname")
    private String memberNickname;
    @Column(name = "create_time")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createTime;
    @Column(name = "member_status")
    private Status memberStatus;
}
