package com.menstalk.billqueryservice.dto;

import com.menstalk.billqueryservice.models.BillDetailType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillDetailResponse {
    private Long billDetailId;
    private Long billId;
    private Long memberId;
    private BillDetailType billDetailType;
    private Long amount;
}
