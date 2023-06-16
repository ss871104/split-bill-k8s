package com.menstalk.mastercommandservice.dto;

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

