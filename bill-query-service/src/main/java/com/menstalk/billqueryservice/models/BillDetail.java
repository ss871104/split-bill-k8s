package com.menstalk.billqueryservice.models;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bill_detail")
public class BillDetail {

    @Id
    @Column(name="bill_detail_id")
    private Long billDetailId;
    @Column(name="bill_id")
    private Long billId;
    @Column(name="member_id")
    private Long memberId;
    @Column(name="bill_detail_type")
    @Enumerated(EnumType.ORDINAL)
    private BillDetailType billDetailType;
    private Long amount;
}
