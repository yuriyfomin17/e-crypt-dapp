package com.nimofy.customerserver.repository.transaction.crypto;

import com.nimofy.customerserver.model.transaction.crypto.CryptoTransaction;
import com.nimofy.customerserver.model.transaction.crypto.State;
import com.nimofy.customerserver.model.transaction.crypto.Type;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CryptoTransactionRepository extends JpaRepository<CryptoTransaction, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select bt from CryptoTransaction bt join fetch bt.user where bt.id =:id")
    Optional<CryptoTransaction> findByIdWithLock(Long id);

    @Query("select ct from CryptoTransaction ct join fetch ct.user where ct.state in :states and ct.type =:type order by ct.id asc")
    List<CryptoTransaction> fetchTxByStateInAndType(Set<State> states, Type type);

    @Transactional(readOnly = true)
    @Query(nativeQuery = true, value = "select * from crypto_transactions ct where ct.user_id =:userId order by ct.id desc limit 10")
    List<CryptoTransaction> findTenNewestTransactions(Long userId);

    Boolean existsByHash(String hash);
}