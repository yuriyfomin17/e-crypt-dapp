package com.nimofy.customerserver.repository.transaction.bet;

import com.nimofy.customerserver.model.bet.Bet;
import com.nimofy.customerserver.model.transaction.bet.BetTransaction;

import java.util.List;

public interface CustomBetTransactionRepository {

    void batchUpdateUserBalanceBasedOnGameOutcome(List<BetTransaction> transactions, Bet bet);

    void batchResetUserBalance(List<BetTransaction> filteredGameTransactions);
}