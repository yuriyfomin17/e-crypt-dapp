package com.nimofy.customerserver.service.users;

import com.nimofy.customerserver.converter.BetConverter;
import com.nimofy.customerserver.converter.CryptoTxConverter;
import com.nimofy.customerserver.dto.bet.BetDto;
import com.nimofy.customerserver.dto.bet.BetTransactionDto;
import com.nimofy.customerserver.dto.response.ResponseDto;
import com.nimofy.customerserver.dto.response.ResponseType;
import com.nimofy.customerserver.dto.response.TransactionResponseDto;
import com.nimofy.customerserver.dto.user.BalanceDto;
import com.nimofy.customerserver.model.Outcome;
import com.nimofy.customerserver.model.bet.Bet;
import com.nimofy.customerserver.model.bet.BetStatus;
import com.nimofy.customerserver.model.transaction.bet.BetTransaction;
import com.nimofy.customerserver.model.user.User;
import com.nimofy.customerserver.repository.bet.BetRepository;
import com.nimofy.customerserver.repository.transaction.bet.BetTransactionRepository;
import com.nimofy.customerserver.repository.user.UserRepository;
import com.nimofy.customerserver.service.payment.BigDecimalCalculator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.nimofy.customerserver.dto.response.ResponseMessage.*;
import static com.nimofy.customerserver.dto.response.ResponseType.ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBetService {

    private final BetRepository betRepository;
    private final UserRepository userRepository;
    private final BetTransactionRepository betTransactionRepository;
    private final BigDecimalCalculator bigDecimalCalculator;
    private final BetConverter betConverter;
    private final CryptoTxConverter cryptoTxConverter;
    @Value("${game.number.limit}")
    private Long GAME_LIMIT;

    public List<BetDto> fetchUserAccountBets(Long userId) {
        return betRepository.findByUserId(userId)
                .stream()
                .map(betConverter::convert)
                .toList();
    }


    @Transactional
    public ResponseDto linkGameToUser(Long userId, Long betId) {
        if (betRepository.existsByUserIdAndBetId(userId, betId)) {
            return ResponseDto.builder()
                    .responseType(GAME_IS_ALREADY_LINKED_ERROR)
                    .type(ERROR)
                    .build();
        }
        if (betRepository.countUserBetSize(userId).compareTo(GAME_LIMIT) >= 0) {
            return ResponseDto.builder()
                    .responseType(GAME_LIMIT_EXCEEDED_ERROR)
                    .type(ERROR)
                    .build();
        }
        betRepository.linkUserToBet(userId, betId);
        Bet bet = betRepository.findById(betId).orElseThrow(() -> new RuntimeException(String.format("BetId [%d] does not exist", betId)));
        return ResponseDto.builder()
                .responseType(TOAST_GAME_ADDITION)
                .betDto(betConverter.convert(bet))
                .type(ResponseType.SUCCESS)
                .build();

    }

    @Transactional
    public void unlinkBetFromUser(Long userId, Long betId) {
        log.info("unlinking betId [{}] from userId [{}]", betId, userId);
        betRepository.unlinkUserFromBet(userId, betId);
    }

    public Optional<BalanceDto> findUserBalanceData(Long userId) {
        return userRepository.findUserDataByUserId(userId);
    }

    @Transactional
    public TransactionResponseDto isTransactionCreated(BetTransactionDto betTransactionDto) {
        Bet bet = getBetWithLock(betTransactionDto.getBetId());
        User user = getUserWithLock(betTransactionDto.getUserId());
        if (bigDecimalCalculator.isBiggerThen(bet.getEthTicketPrice(), user.getEthBalance())) {
            log.error("userId [{}] does not have enough balance for betId [{}]", betTransactionDto.getUserId(), betTransactionDto.getBetId());
            return new TransactionResponseDto(NOT_ENOUGH_BALANCE_ERROR, null);
        }
        if (bet.getStatus() != BetStatus.OPENED) {
            log.error("userId [{}] can not place bet. game already finished [{}]", betTransactionDto.getUserId(), betTransactionDto.getBetId());
            return new TransactionResponseDto(GAME_ALREADY_FINISHED_ERROR, null);
        }
        user.setEthBalance(bigDecimalCalculator.subtract(user.getEthBalance(), bet.getEthTicketPrice()));
        betTransactionRepository.save(BetTransaction.builder()
                .bet(bet)
                .user(user)
                .predictedOutcome(betTransactionDto.getPredictedOutcome())
                .transactionOutcome(Outcome.NOT_DETERMINED)
                .ethAmount(bet.getEthTicketPrice())
                .build()
        );
        switch (betTransactionDto.getPredictedOutcome()) {
            case WIN -> {
                bet.setEthWinStakeAmount(bigDecimalCalculator.add(bet.getEthWinStakeAmount(), bet.getEthTicketPrice()));
                bet.setBetsForWinCount(bet.getBetsForWinCount() + 1);
            }
            case LOSE -> {
                bet.setEthLossStakeAmount(bigDecimalCalculator.add(bet.getEthLossStakeAmount(), bet.getEthTicketPrice()));
                bet.setBetsForLossCount(bet.getBetsForLossCount() + 1);
            }
        }
        return new TransactionResponseDto(RESPONSE_SUCCESS, bet);
    }

    public List<BetTransactionDto> latestTenGameTransactions(Long userId) {
        return betTransactionRepository.findTenNewestTransactions(userId)
                .stream()
                .map(cryptoTxConverter::convert)
                .toList();

    }

    private Bet getBetWithLock(Long betId) {
        return betRepository.findByIdWithLock(betId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Bet with Id:[%d] does not exist", betId)));
    }

    private User getUserWithLock(Long userId) {
        return userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with Id:[%d] does not exist", userId)));
    }
}