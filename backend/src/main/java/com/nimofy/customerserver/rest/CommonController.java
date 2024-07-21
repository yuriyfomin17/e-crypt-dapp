package com.nimofy.customerserver.rest;

import com.nimofy.customerserver.dto.bet.BetDto;
import com.nimofy.customerserver.dto.bet.BetTransactionDto;
import com.nimofy.customerserver.dto.crypto.UserTransactionDto;
import com.nimofy.customerserver.dto.response.ResponseDto;
import com.nimofy.customerserver.dto.user.BalanceDto;
import com.nimofy.customerserver.dto.user.UserDto;
import com.nimofy.customerserver.service.auth.AuthService;
import com.nimofy.customerserver.service.auth.JwtService;
import com.nimofy.customerserver.service.payment.BigDecimalCalculator;
import com.nimofy.customerserver.service.payment.CryptoPaymentService;
import com.nimofy.customerserver.service.payment.CryptoTransactionService;
import com.nimofy.customerserver.service.payment.Web3Service;
import com.nimofy.customerserver.service.users.UserBetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.nimofy.customerserver.dto.response.ResponseMessage.NUMBER_SHOULD_BE_NUMERIC_AND_GREATER_THEN_ZERO_ERROR;

@RestController
@RequestMapping("/v1/all-bet/common")
@RequiredArgsConstructor
@Slf4j
public class CommonController {

    private static final String BEARER_PREFIX = "Bearer ";
    private final CryptoPaymentService cryptoPaymentService;
    private final UserBetService userBetService;
    private final CryptoTransactionService cryptoTransactionService;
    private final Web3Service web3Service;
    private final BigDecimalCalculator bigDecimalCalculator;
    private final AuthService authService;
    private final JwtService jwtService;

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping("/linkWallet")
    public ResponseEntity<HttpStatus> linkWallet(@RequestHeader(value = "userId") Long userId, @RequestHeader(value = "Address") String address) {
        boolean isUserAddressLinked = authService.checkOrLinkUserWalletAddress(userId, address);
        return isUserAddressLinked ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/refresh")
    public ResponseEntity<UserDto> refreshToken(@RequestHeader(value = "userId") Long userId, @RequestHeader(value = "Authorization") String oldToken) {
        String oldPasswordWithSalt = jwtService.getPassword(StringUtils.removeStartIgnoreCase(oldToken, BEARER_PREFIX));
        UserDto userDto = authService.refreshUserToken(userId, oldPasswordWithSalt);
        if (userDto == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/price/usd")
    public ResponseEntity<Double> getApproximateEthUsdPrice() {
        return ResponseEntity.ok(web3Service.getEthUsdRate());
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/transactions")
    public ResponseEntity<List<UserTransactionDto>> getTenLatestCryptoTransactions(@RequestHeader(value = "userid") Long userId) {
        return ResponseEntity.ok(cryptoPaymentService.latestTenCryptoTransactions(userId));
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/game/transactions")
    public ResponseEntity<List<BetTransactionDto>> getTenLatestGameTransactions(@RequestHeader(value = "userid") Long userId){
        return ResponseEntity.ok(userBetService.latestTenGameTransactions(userId));
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/bets")
    public ResponseEntity<List<BetDto>> fetchUserBets(@RequestHeader(value = "userid") Long userId) {
        return ResponseEntity.ok(userBetService.fetchUserAccountBets(userId));
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/create/deposit")
    public ResponseEntity<ResponseDto> createDepositTx(@RequestBody UserTransactionDto userTransactionDto) {
        return ResponseEntity.ok(cryptoTransactionService.createDepositTx(userTransactionDto));
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/create/withdraw")
    public ResponseEntity<ResponseDto> createWithdrawCryptoTransaction(@RequestBody UserTransactionDto userTransactionDto) {
        if (bigDecimalCalculator.isNotNumericOrLessOrEqualZero(userTransactionDto.getEthAmount())) {
            return ResponseDto.createOkResponse(NUMBER_SHOULD_BE_NUMERIC_AND_GREATER_THEN_ZERO_ERROR);
        }
        if (cryptoPaymentService.isSignatureIsVerified(userTransactionDto)) {
            authService.updatePassword(userTransactionDto.getUserId());
            return ResponseEntity.ok(cryptoPaymentService.createWithdrawTxUpdateCache(userTransactionDto));
        }
        return ResponseEntity
                .badRequest()
                .build();
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/account/userData")
    public ResponseEntity<BalanceDto> getUserData(@RequestHeader(value = "userid") Long userId) {
        return userBetService.findUserBalanceData(userId)
                .map(balanceDto -> ResponseEntity.ok(BalanceDto.stringifyBigDecimal(balanceDto)))
                .orElse(ResponseEntity.noContent().build());
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping("/unlink/bet/{userId}/{betId}")
    public ResponseEntity<HttpStatus> unlinkBet(@PathVariable Long userId, @PathVariable Long betId) {
        userBetService.unlinkBetFromUser(userId, betId);
        return ResponseEntity.ok().build();
    }
}