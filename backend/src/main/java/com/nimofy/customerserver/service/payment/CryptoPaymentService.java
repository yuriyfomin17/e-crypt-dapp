package com.nimofy.customerserver.service.payment;

import com.nimofy.customerserver.converter.CryptoTxConverter;
import com.nimofy.customerserver.dto.crypto.UserTransactionDto;
import com.nimofy.customerserver.dto.response.ResponseDto;
import com.nimofy.customerserver.dto.user.UserWalletSaltDto;
import com.nimofy.customerserver.repository.transaction.crypto.CryptoTransactionRepository;
import com.nimofy.customerserver.repository.user.UserRepository;
import com.nimofy.customerserver.service.finance.FinanceService;
import com.nimofy.customerserver.util.CryptographyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.nimofy.customerserver.dto.response.ResponseDto.createErrorResponse;
import static com.nimofy.customerserver.dto.response.ResponseMessage.*;
import static com.nimofy.customerserver.dto.response.ResponseType.ERROR;
import static com.nimofy.customerserver.dto.response.ResponseType.SUCCESS;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoPaymentService {

    @Value("${minimum.withdraw.amount}")
    private BigDecimal MINIMUM_WITHDRAW_AMOUNT;

    private final BigDecimalCalculator bigDecimalCalculator;
    private final CryptoTransactionRepository cryptoTransactionRepository;
    private final CryptoTxConverter cryptoTxConverter;
    private final FinanceService financeService;
    private final CryptoTransactionService cryptoTransactionService;
    private final UserRepository userRepository;
    private final Web3Service web3Service;
    private final CryptographyUtil cryptographyUtil;

    private static final String CONFIRMATION_WITHDRAW_MESSAGE = "Confirm Withdraw Operation ";

    public ResponseDto createWithdrawTxUpdateCache(UserTransactionDto withdrawDto) {
        BigDecimal ethAmountToWithDraw = bigDecimalCalculator.convertStringToBigDecimal(withdrawDto.getEthAmount());

        boolean isGreaterThenMinimumWithdrawAmount = bigDecimalCalculator.isEqualOrGreaterThen(ethAmountToWithDraw, MINIMUM_WITHDRAW_AMOUNT);
        if (!isGreaterThenMinimumWithdrawAmount) {
            log.error("userId [{}] does not pass minimum withdraw threshold amount [{}]", withdrawDto.getUserId(), ethAmountToWithDraw.toPlainString());
            return ResponseDto.builder()
                    .responseType(MINIMUM_WITHDRAW_AMOUNT_ERROR)
                    .type(ERROR)
                    .build();
        }
        boolean isBalanceUpdated = financeService.checkDecreaseBalance(withdrawDto.getUserId(), ethAmountToWithDraw);
        if (!isBalanceUpdated) {
            log.error("userId [{}] does not have enough funds to withdraw [{}]", withdrawDto.getUserId(), ethAmountToWithDraw);
            return ResponseDto.builder()
                    .responseType(NOT_ENOUGH_BALANCE_ERROR)
                    .type(ERROR)
                    .build();
        }
        boolean isTxCreated = web3Service.createWithdrawTx(withdrawDto.getAddress(), ethAmountToWithDraw)
                .map(transactionHash -> handleTransactionHashId(transactionHash, withdrawDto.getUserId(), ethAmountToWithDraw))
                .orElseGet(() -> handleTransactionError(withdrawDto.getUserId(), ethAmountToWithDraw));
        return isTxCreated ?
                ResponseDto.builder()
                        .responseType(WITHDRAW_TRANSACTION_CREATED)
                        .type(SUCCESS)
                        .build() : createErrorResponse();
    }

    public List<UserTransactionDto> latestTenCryptoTransactions(Long userId) {
        return cryptoTransactionRepository.findTenNewestTransactions(userId)
                .stream()
                .map(cryptoTxConverter::convert)
                .toList();
    }

    public boolean isSignatureIsVerified(UserTransactionDto withdrawDto) {
        UserWalletSaltDto userWalletSaltDto = userRepository.findWalletAddressByUserId(withdrawDto.getUserId()).orElseThrow(() -> new RuntimeException(String.format("user with id %d does not exist", withdrawDto.getUserId())));
        String derivedAddress = "0x" + cryptographyUtil.getAddressUsedToSignHashedMessage(withdrawDto.getSignature(), CONFIRMATION_WITHDRAW_MESSAGE + userWalletSaltDto.tokenSalt());
        return withdrawDto.getAddress() != null && userWalletSaltDto.walletAddress().equalsIgnoreCase(withdrawDto.getAddress()) && derivedAddress.equalsIgnoreCase(withdrawDto.getAddress());
    }

    private boolean handleTransactionHashId(String txHash, Long userId, BigDecimal amountToWithdraw) {
        cryptoTransactionService.createWithdrawTx(txHash, userId, amountToWithdraw);
        return true;
    }

    private boolean handleTransactionError(Long userId, BigDecimal ethAmountToWithDraw) {
        financeService.increaseUserBalance(userId, ethAmountToWithDraw);
        return false;
    }
}