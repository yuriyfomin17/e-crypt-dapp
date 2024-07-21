package com.nimofy.customerserver.converter;

import com.nimofy.customerserver.dto.bet.BetDto;
import com.nimofy.customerserver.model.bet.Bet;
import com.nimofy.customerserver.model.bet.BetStatus;
import com.nimofy.customerserver.service.payment.BigDecimalCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BetConverter {

    private final BigDecimalCalculator bigDecimalCalculator;

    public BetDto convert(Bet bet) {
        return BetDto.builder()
                .id(bet.getId())
                .title(bet.getTitle())
                .status(bet.getStatus())
                .outcome(bet.getOutcome())
                .ethWinStakeAmount(bet.getEthWinStakeAmount().toPlainString())
                .ethLossStakeAmount(bet.getEthLossStakeAmount().toPlainString())
                .betsForWinCount(bet.getBetsForWinCount())
                .betsForLossCount(bet.getBetsForLossCount())
                .ethTicketPrice(bet.getEthTicketPrice().toPlainString())
                .estimatedEthAdminProfit(bigDecimalCalculator.estimateAdminProfit(bet.getEthWinStakeAmount(), bet.getEthLossStakeAmount()).toPlainString())
                .ethProfit(bet.getEthProfit().toPlainString())
                .createdAt(bet.getCreatedAt())
                .build();
    }

    public Bet convert(BetDto betDto) {
        BetStatus betStatus = betDto.getStatus() == null ? BetStatus.CREATED: betDto.getStatus();
        return Bet.builder()
                .id(betDto.getId())
                .title(betDto.getTitle())
                .status(betStatus)
                .ethTicketPrice(bigDecimalCalculator.convertStringToBigDecimal(betDto.getEthTicketPrice()))
                .adminUserId(betDto.getAdminUserId())
                .build();
    }
}