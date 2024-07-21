package com.nimofy.customerserver.repository.transaction.bet;

import com.nimofy.customerserver.model.Outcome;
import com.nimofy.customerserver.model.bet.Bet;
import com.nimofy.customerserver.model.transaction.bet.BetTransaction;
import com.nimofy.customerserver.service.bet.PaymentService;
import com.nimofy.customerserver.service.payment.BigDecimalCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CustomBetTransactionRepositoryImpl implements CustomBetTransactionRepository {
    private final JdbcTemplate jdbcTemplate;
    private final BigDecimalCalculator bigDecimalCalculator;
    private final PaymentService paymentService;

    @Transactional
    @Override
    public void batchUpdateUserBalanceBasedOnGameOutcome(List<BetTransaction> transactions, Bet bet) {
        if (transactions.isEmpty()) {
            return;
        }
        Map<Boolean, List<BetTransaction>> isWinningBetTransactionMap = transactions.stream().collect(Collectors.groupingBy(tx -> tx.getPredictedOutcome() == bet.getOutcome(), Collectors.toList()));
        List<BetTransaction> losingTxs = isWinningBetTransactionMap.get(Boolean.FALSE);
        if (!losingTxs.isEmpty()) {
            jdbcTemplate.batchUpdate("UPDATE bet_transactions SET transaction_outcome = ? where id = ?",
                    losingTxs,
                    50,
                    (PreparedStatement ps, BetTransaction tx) -> {
                        ps.setString(1, Outcome.LOSE.name());
                        ps.setLong(2, tx.getId());
                    });
        }
        List<BetTransaction> winningTxs = isWinningBetTransactionMap.get(Boolean.TRUE);
        if (!winningTxs.isEmpty()) {
            BigDecimal transactionProfit = paymentService.calculateTransactionProfit(bet);
            jdbcTemplate.batchUpdate("UPDATE bet_transactions SET transaction_outcome = ?, eth_profit = ? where id = ?",
                    winningTxs,
                    50,
                    (PreparedStatement ps, BetTransaction tx) -> {
                        ps.setString(1, Outcome.WIN.name());
                        ps.setBigDecimal(2, transactionProfit);
                        ps.setLong(3, tx.getId());
                    });
            jdbcTemplate.batchUpdate("UPDATE users SET eth_balance = eth_balance + ?, total_eth_profit_earned = total_eth_profit_earned + ? where id = ?",
                    winningTxs,
                    50,
                    (PreparedStatement ps, BetTransaction tx) -> {
                        Long userId = tx.getUser().getId();
                        ps.setBigDecimal(1, bigDecimalCalculator.add(transactionProfit, tx.getEthAmount()));
                        ps.setBigDecimal(2, transactionProfit);
                        ps.setLong(3, userId);
                    });
        }
    }

    @Transactional
    @Override
    public void batchResetUserBalance(List<BetTransaction> filteredGameTransactions) {
        if (filteredGameTransactions.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate("UPDATE users SET eth_balance = eth_balance + ? where id = ?",
                filteredGameTransactions,
                50,
                (PreparedStatement ps, BetTransaction tx) -> {
                    Long userId = tx.getUser().getId();
                    ps.setBigDecimal(1, tx.getEthAmount());
                    ps.setBigDecimal(2, tx.getEthAmount());
                    ps.setLong(3, userId);
                });
    }
}