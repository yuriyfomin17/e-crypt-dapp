package com.nimofy.customerserver.repository.user;

import com.nimofy.customerserver.dto.user.BalanceDto;
import com.nimofy.customerserver.dto.user.UserWalletSaltDto;
import com.nimofy.customerserver.model.user.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id =:userId")
    Optional<User> findByIdWithLock(Long userId);

    @Query("select distinct u from User u left join fetch u.bets where u.email =:email ")
    Optional<User> findByEmailFetchBets(String email);

    @Query("select new com.nimofy.customerserver.dto.user.UserWalletSaltDto(u.walletAddress, u.password) from User u where u.id =:userId")
    Optional<UserWalletSaltDto> findWalletAddressByUserId(Long userId);

    @Transactional(readOnly = true)
    @Query("select u.ethBalance from User u where u.id =:userId")
    BigDecimal ethBalanceByUserId(Long userId);

    @Query("select new com.nimofy.customerserver.dto.user.BalanceDto(u.ethBalance, u.totalEthProfitEarned, u.password) from User  u where u.id =:userId ")
    Optional<BalanceDto> findUserDataByUserId(long userId);
}