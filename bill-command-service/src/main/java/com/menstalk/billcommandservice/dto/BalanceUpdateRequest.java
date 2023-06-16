package com.menstalk.billcommandservice.dto;

import com.menstalk.billcommandservice.models.BillDetailType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BalanceUpdateRequest {

	private BillDetailType billDetailType;
	private Long memberId;
	private Long amount;
}

