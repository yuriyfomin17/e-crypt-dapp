package com.nimofy.customerserver.service.bet;

import com.nimofy.customerserver.model.bet.Bet;
import com.nimofy.customerserver.model.bet.BetStatus;
import com.nimofy.customerserver.model.user.User;
import com.nimofy.customerserver.repository.user.UserRepository;
import com.nimofy.customerserver.service.payment.BigDecimalCalculator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    @Value("${nimofy.user.id}")
    private Long NIMOFY_ID;

    private final UserRepository userRepository;
    private final BigDecimalCalculator bigDecimalCalculator;

    public boolean isValidBet(Bet bet) {
        return bet.getStatus() == BetStatus.RESOLVED &&
                bigDecimalCalculator.isBiggerThenZero(bet.getEthWinStakeAmount()) &&
                bigDecimalCalculator.isBiggerThenZero(bet.getEthLossStakeAmount());
    }

    public BigDecimal calculateTransactionProfit(Bet bet) {
        BigDecimal winOutcomeProfit = bigDecimalCalculator.calculateTxProfit(bet.getEthLossStakeAmount(), bet.getBetsForWinCount());
        BigDecimal loseOutcomeProfit = bigDecimalCalculator.calculateTxProfit(bet.getEthWinStakeAmount(), bet.getBetsForLossCount());
        return switch (bet.getOutcome()) {
            case WIN -> winOutcomeProfit;
            case LOSE -> loseOutcomeProfit;
            case NOT_DETERMINED -> BigDecimal.ZERO;
        };
    }

    public void setAdminAndNimofyBetProfit(Bet bet) {
        User adminUser = userRepository.findByIdWithLock(bet.getAdminUserId()).orElseThrow(() -> new RuntimeException(String.format("User with Id [%d] does not exist", bet.getAdminUserId())));
        BigDecimal adminPositiveOutcomeProfit = bigDecimalCalculator.calculateAdminProfit(bet.getEthWinStakeAmount());
        BigDecimal adminNegativeOutcomeProfit = bigDecimalCalculator.calculateAdminProfit(bet.getEthLossStakeAmount());

        BigDecimal nimofyPositiveOutcomeProfit = bigDecimalCalculator.calculateNimofyProfit(bet.getEthWinStakeAmount());
        BigDecimal nimofyNegativeOutcomeProfit = bigDecimalCalculator.calculateNimofyProfit(bet.getEthLossStakeAmount());
        switch (bet.getOutcome()) {
            case WIN ->
                    updateNimofyAndAdminProfit(bet, adminUser, adminNegativeOutcomeProfit, nimofyNegativeOutcomeProfit);
            case LOSE ->
                    updateNimofyAndAdminProfit(bet, adminUser, adminPositiveOutcomeProfit, nimofyPositiveOutcomeProfit);
        }
    }

    private void updateNimofyAndAdminProfit(Bet bet, User adminUser, BigDecimal adminBetProfit, BigDecimal nimofyBetProfit) {
        bet.setEthProfit(adminBetProfit);
        adminUser.setEthBalance(bigDecimalCalculator.add(adminUser.getEthBalance(), adminBetProfit));
        adminUser.setTotalEthProfitEarned(bigDecimalCalculator.add(adminUser.getTotalEthProfitEarned(), adminBetProfit));

        User nimofyUser = userRepository.findByIdWithLock(NIMOFY_ID).orElseThrow(() -> new EntityNotFoundException(String.format("User with id [%d] does not exist", NIMOFY_ID)));
        nimofyUser.setEthBalance(bigDecimalCalculator.add(nimofyUser.getEthBalance(), nimofyBetProfit));
        nimofyUser.setTotalEthProfitEarned(bigDecimalCalculator.add(nimofyUser.getTotalEthProfitEarned(), nimofyBetProfit));
    }
}