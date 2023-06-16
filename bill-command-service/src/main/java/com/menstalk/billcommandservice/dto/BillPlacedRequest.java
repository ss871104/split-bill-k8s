package com.menstalk.billcommandservice.dto;

import com.menstalk.billcommandservice.models.BillType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillPlacedRequest {
    private Long partyId;
    private String billName;
    private BillType billType;
    private Long totalAmount;
    private Map<Long, Long> memberIdMapExpense;
    private Map<Long, Long> memberIdMapIncome;

}
