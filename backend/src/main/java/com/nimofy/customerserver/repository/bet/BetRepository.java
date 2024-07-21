package com.nimofy.customerserver.repository.bet;

import com.nimofy.customerserver.model.bet.Bet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

public interface BetRepository extends JpaRepository<Bet, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bet b where b.id =:id")
    Optional<Bet> findByIdWithLock(Long id);

    @Transactional(readOnly = true)
    @Query(nativeQuery = true, value = "select * from bets b where b.id in (select ub.bet_id from user_bet ub where ub.user_id =:userId order by ub.bet_id desc limit 10)")
    Set<Bet> findByUserId(Long userId);

    @Transactional(readOnly = true)
    @Query(nativeQuery = true, value = "select ub.user_id from user_bet ub where bet_id =:betId")
    Set<Long> findParticipantsIds(Long betId);

    @Query("select b from Bet b join fetch b.participants where b.id =:betId")
    Optional<Bet> fetchBetParticipants(Long betId);

    @Query(nativeQuery = true, value = "select count (*) from user_bet ub where ub.user_id =:userId")
    Long countUserBetSize(Long userId);

    @Query(nativeQuery = true, value = "select exists( select ub from user_bet ub where ub.user_id =:userId and ub.bet_id =:betId)")
    boolean existsByUserIdAndBetId(Long userId, Long betId);

    @Modifying
    @Query(nativeQuery = true, value = "insert into user_bet (user_id, bet_id) values (?1, ?2);")
    void linkUserToBet(Long userId, Long betId);

    @Modifying
    @Query(nativeQuery = true, value = "delete from user_bet where user_id =:userId and bet_id =:betId")
    void unlinkUserFromBet(Long userId, Long betId);
}