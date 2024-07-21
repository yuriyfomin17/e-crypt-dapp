package com.nimofy.customerserver.converter;

import com.nimofy.customerserver.dto.bet.BetTransactionDto;
import com.nimofy.customerserver.dto.crypto.UserTransactionDto;
import com.nimofy.customerserver.model.transaction.bet.BetTransaction;
import com.nimofy.customerserver.model.transaction.crypto.CryptoTransaction;
import com.nimofy.customerserver.service.payment.BigDecimalCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CryptoTxConverter {

    private final BigDecimalCalculator bigDecimalCalculator;

    public UserTransactionDto convert(CryptoTransaction cryptoTransaction) {
        String ethAmount = cryptoTransaction.getEthAmount() == null ? BigDecimal.ZERO.toPlainString() : cryptoTransaction.getEthAmount().toPlainString();
        return UserTransactionDto.builder()
                .id(cryptoTransaction.getId())
                .hash(cryptoTransaction.getHash())
                .state(cryptoTransaction.getState())
                .percentage(bigDecimalCalculator.getConfirmationsPercentage(cryptoTransaction))
                .ethAmount(ethAmount)
                .type(cryptoTransaction.getType())
                .createdAt(cryptoTransaction.getCreatedAt())
                .build();
    }

    public BetTransactionDto convert(BetTransaction betTransaction){
        return BetTransactionDto.builder()
                .id(betTransaction.getId())
                .ethAmount(betTransaction.getEthAmount().toPlainString())
                .ethProfit(betTransaction.getEthProfit().toPlainString())
                .predictedOutcome(betTransaction.getPredictedOutcome())
                .transactionOutcome(betTransaction.getTransactionOutcome())
                .createdAt(betTransaction.getCreatedAt())
                .build();
    }
}