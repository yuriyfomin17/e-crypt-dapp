package com.nimofy.customerserver.dto.bet;

import com.nimofy.customerserver.model.Outcome;
import com.nimofy.customerserver.model.bet.BetStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import static com.nimofy.customerserver.dto.UpdateType.BET_INFORMATION;

@Data
@Builder
public class BetDto {
    private Long id;
    private String title;
    private Long adminUserId;
    private String ethWinStakeAmount;
    private String ethLossStakeAmount;
    private String ethTicketPrice;
    private String estimatedEthAdminProfit;
    private String ethProfit;
    private Long betsForWinCount;
    private Long betsForLossCount;
    @Builder.Default
    private BetStatus status = BetStatus.CREATED;
    @Builder.Default
    private Outcome outcome = Outcome.NOT_DETERMINED;
    private LocalDateTime createdAt;

    public String toJson() {
        return String.format("""
                        {
                            "id":%d,
                            "updateType": "%s",
                            "status": "%s",
                            "outcome": "%s",
                            "ethWinStakeAmount": "%s",
                            "ethLossStakeAmount": "%s",
                            "estimatedEthAdminProfit":"%s",
                            "betsForWinCount": %d,
                            "betsForLossCount": %d,
                            "ethProfit": "%s"
                        }
                        """,
                getId(),
                BET_INFORMATION.name(),
                getStatus().name(),
                getOutcome().name(),
                getEthWinStakeAmount(),
                getEthLossStakeAmount(),
                getEstimatedEthAdminProfit(),
                getBetsForWinCount(),
                getBetsForLossCount(),
                getEthProfit()
        );
    }
}