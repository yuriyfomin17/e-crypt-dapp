package com.nimofy.customerserver.service.finance;

import com.nimofy.customerserver.model.user.User;
import com.nimofy.customerserver.repository.user.UserRepository;
import com.nimofy.customerserver.service.payment.BigDecimalCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final UserRepository userRepository;
    private final BigDecimalCalculator bigDecimalCalculator;

    @Transactional
    public boolean checkDecreaseBalance(Long userId, BigDecimal ethAmount) {
        return userRepository.findByIdWithLock(userId)
                .map(user -> isBalanceUpdated(user, ethAmount, false))
                .orElse(false);
    }

    @Transactional
    public void increaseUserBalance(Long userId, BigDecimal ethAmount){
        userRepository.findByIdWithLock(userId).ifPresent(user -> isBalanceUpdated(user, ethAmount, true));
    }

    private boolean isBalanceUpdated(User user, BigDecimal ethAmount, boolean isTopUp) {
        if (isTopUp){
            user.setEthBalance(bigDecimalCalculator.add(user.getEthBalance(), ethAmount));
            return true;
        }
        if (bigDecimalCalculator.isBiggerThen(ethAmount, user.getEthBalance())){
            return false;
        }
        user.setEthBalance(bigDecimalCalculator.subtract(user.getEthBalance(), ethAmount));
        return true;
    }
}