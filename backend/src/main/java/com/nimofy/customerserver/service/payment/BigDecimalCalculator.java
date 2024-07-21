package com.nimofy.customerserver.service.payment;

import com.nimofy.customerserver.model.transaction.crypto.CryptoTransaction;
import com.nimofy.customerserver.model.transaction.crypto.Type;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import static com.nimofy.customerserver.service.payment.CryptoTransactionService.CONFIRMATIONS_THRESHOLD;

@Service
public class BigDecimalCalculator {

    private static final String HUNDRED = "100,00";
    @Value("${user.profit.coefficient}")
    private BigDecimal USER_PROFIT_COEFFICIENT;
    @Value("${admin.profit.coefficient}")
    private BigDecimal ADMIN_PROFIT_COEFFICIENT;
    @Value("${nimofy.profit.coefficient}")
    private BigDecimal NIMOFY_PROFIT_COEFFICIENT;
    @Value("${eth.calculation.scale}")
    private Integer SCALE;

    public BigDecimal estimateAdminProfit(BigDecimal primaryAmount, BigDecimal secondaryAmount) {
        if (isLessOrEqualZero(primaryAmount) || isLessOrEqualZero(secondaryAmount)) {
            return BigDecimal.ZERO;
        }
        return add(primaryAmount.multiply(ADMIN_PROFIT_COEFFICIENT), secondaryAmount.multiply(ADMIN_PROFIT_COEFFICIENT)).divide(BigDecimal.valueOf(2), SCALE, RoundingMode.HALF_DOWN);
    }

    public BigDecimal calculateTxProfit(BigDecimal primaryAmount, Long secondaryAmount) {
        return primaryAmount.multiply(USER_PROFIT_COEFFICIENT).setScale(SCALE, RoundingMode.DOWN).divide(BigDecimal.valueOf(secondaryAmount), SCALE, RoundingMode.DOWN);
    }

    public BigDecimal calculateAdminProfit(BigDecimal amount) {
        return amount.multiply(ADMIN_PROFIT_COEFFICIENT).setScale(SCALE, RoundingMode.DOWN);
    }

    public BigDecimal calculateNimofyProfit(BigDecimal amount){
        return amount.multiply(NIMOFY_PROFIT_COEFFICIENT).setScale(SCALE, RoundingMode.DOWN);
    }

    public BigDecimal add(BigDecimal primaryAmount, BigDecimal secondaryAmount) {
        return primaryAmount.add(secondaryAmount).setScale(SCALE, RoundingMode.DOWN);
    }

    public BigDecimal subtract(BigDecimal primaryAmount, BigDecimal secondaryAmount) {
        return primaryAmount.subtract(secondaryAmount).setScale(SCALE, RoundingMode.DOWN);
    }

    public boolean isBiggerThenZero(BigDecimal amount) {
        return Objects.compare(amount, BigDecimal.ZERO.setScale(SCALE, RoundingMode.DOWN), BigDecimal::compareTo) > 0;
    }

    public boolean isBiggerThen(BigDecimal primaryAmount, BigDecimal secondaryAmount) {
        return Objects.compare(primaryAmount.setScale(SCALE, RoundingMode.DOWN), secondaryAmount.setScale(SCALE, RoundingMode.DOWN), BigDecimal::compareTo) > 0;
    }

    public boolean isEqualOrGreaterThen(BigDecimal primaryAmount, BigDecimal secondaryAmount) {
        return Objects.compare(
                primaryAmount.setScale(SCALE, RoundingMode.DOWN),
                secondaryAmount.setScale(SCALE, RoundingMode.DOWN),
                BigDecimal::compareTo) >= 0;
    }

    public BigDecimal convertStringToBigDecimal(String ethAmount) {
        return new BigDecimal(ethAmount).setScale(SCALE, RoundingMode.DOWN);
    }

    public boolean isNotNumeric(String str) {
        return !str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional '-' and decimal.
    }

    public boolean isLessOrEqualZero(BigDecimal bigDecimal){
        return bigDecimal.compareTo(BigDecimal.ZERO) <= 0;
    }

    public boolean isNotNumericOrLessOrEqualZero(String ethAmount){
        return isNotNumeric(ethAmount) || isLessOrEqualZero(convertStringToBigDecimal(ethAmount));
    }

    public String getConfirmationsPercentage(CryptoTransaction cryptoTransaction){
        if (cryptoTransaction.getType() == Type.WITHDRAW && cryptoTransaction.getConfirmations().compareTo(0L) > 0){
            return HUNDRED;
        }
        if (cryptoTransaction.getConfirmations().compareTo(CONFIRMATIONS_THRESHOLD) > 0){
            return HUNDRED;
        }
        return String.format("%.2f", cryptoTransaction.getConfirmations().doubleValue() * 100 / CONFIRMATIONS_THRESHOLD);
    }
}