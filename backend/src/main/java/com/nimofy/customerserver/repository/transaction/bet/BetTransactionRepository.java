package com.nimofy.customerserver.repository.transaction.bet;

import com.nimofy.customerserver.model.transaction.bet.BetTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BetTransactionRepository extends JpaRepository<BetTransaction, Long>, CustomBetTransactionRepository {

    @Query("select bt from BetTransaction bt join fetch bt.user where bt.bet.id =:betId")
    List<BetTransaction> findByBetIdFetchUser(Long betId);

    @Transactional(readOnly = true)
    @Query(nativeQuery = true, value = "select * from bet_transactions bt where bt.user_id =:userId order by id desc limit 10")
    List<BetTransaction> findTenNewestTransactions(Long userId);
}