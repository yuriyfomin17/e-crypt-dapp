package com.nimofy.customerserver.dto.user;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Data
public class BalanceDto {
    private final BigDecimal ethBalanceNumeric;
    private final BigDecimal totalEthProfitEarnedNumeric;
    private final String tokenSalt;

    private String ethBalance;
    private String totalEthProfitEarned;

    public static BalanceDto stringifyBigDecimal(BalanceDto balanceDto){
        balanceDto.setEthBalance(balanceDto.getEthBalanceNumeric().toPlainString());
        balanceDto.setTotalEthProfitEarned(balanceDto.getTotalEthProfitEarnedNumeric().toPlainString());
        return balanceDto;
    }
}