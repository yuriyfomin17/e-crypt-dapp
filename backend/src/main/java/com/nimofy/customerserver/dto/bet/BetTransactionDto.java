package com.nimofy.customerserver.dto.bet;

import com.nimofy.customerserver.model.Outcome;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class BetTransactionDto {
    private Long id;
    private Long userId;
    private Long betId;
    private String ethAmount;
    private String ethProfit;
    private Outcome predictedOutcome;
    private Outcome transactionOutcome;
    private LocalDateTime createdAt;
}