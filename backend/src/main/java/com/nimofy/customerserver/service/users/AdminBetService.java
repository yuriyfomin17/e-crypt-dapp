package com.nimofy.customerserver.service.users;

import com.nimofy.customerserver.converter.BetConverter;
import com.nimofy.customerserver.dto.bet.BetDto;
import com.nimofy.customerserver.dto.response.ResponseDto;
import com.nimofy.customerserver.dto.response.ResponseType;
import com.nimofy.customerserver.model.Outcome;
import com.nimofy.customerserver.model.bet.Bet;
import com.nimofy.customerserver.model.bet.BetStatus;
import com.nimofy.customerserver.model.transaction.bet.BetTransaction;
import com.nimofy.customerserver.repository.bet.BetRepository;
import com.nimofy.customerserver.repository.transaction.bet.BetTransactionRepository;
import com.nimofy.customerserver.service.bet.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.nimofy.customerserver.dto.response.ResponseMessage.ADMIN_GAME_CREATION;
import static com.nimofy.customerserver.dto.response.ResponseMessage.GAME_LIMIT_EXCEEDED_ERROR;
import static com.nimofy.customerserver.dto.response.ResponseType.ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBetService {
    private final BetRepository betRepository;
    private final BetConverter betConverter;
    private final PaymentService paymentService;
    private final BetTransactionRepository betTransactionRepository;

    @Value("${game.number.limit}")
    private Long GAME_LIMIT;

    @Transactional
    public ResponseDto createAdminBet(BetDto betDto) {
        Long userBetSize = betRepository.countUserBetSize(betDto.getAdminUserId());
        if (userBetSize.compareTo(GAME_LIMIT) >= 0) {
            return ResponseDto.builder()
                    .responseType(GAME_LIMIT_EXCEEDED_ERROR)
                    .type(ERROR)
                    .build();
        }
        Bet unsavedBet = betConverter.convert(betDto);
        betRepository.save(unsavedBet);
        betRepository.linkUserToBet(betDto.getAdminUserId(), unsavedBet.getId());
        return ResponseDto.builder()
                .responseType(ADMIN_GAME_CREATION)
                .type(ResponseType.SUCCESS)
                .betDto(betConverter.convert(unsavedBet))
                .build();
    }

    @Transactional
    public Bet changeAdminBetStatus(BetDto betDto) {
        log.info("changing betId [{}] to status [{}]", betDto.getId(), betDto.getStatus());
        Bet bet = betRepository.findByIdWithLock(betDto.getId()).orElseThrow(() -> new RuntimeException(String.format("BetId [%d] does not exist", betDto.getId())));
        if (bet.getStatus() == betDto.getStatus()) {
            return null;
        }
        bet.setOutcome(betDto.getOutcome());
        bet.setUpdatedByAdminAt(LocalDateTime.now());
        switch (betDto.getStatus()) {
            case OPENED -> bet.setStatus(BetStatus.OPENED);
            case CLOSED -> bet.setStatus(BetStatus.CLOSED);
            case RESOLVED -> {
                bet.setStatus(BetStatus.RESOLVED);
                beginPayout(bet);
            }
        }
        return bet;
    }

    private void beginPayout(Bet bet) {
        List<BetTransaction> filteredGameTransactions = betTransactionRepository
                .findByBetIdFetchUser(bet.getId()).stream()
                .filter(tx -> tx.getPredictedOutcome() != Outcome.NOT_DETERMINED)
                .toList();
        log.info("found [{}] bet transactions for betId [{}]", filteredGameTransactions.size(), bet.getId());
        if (paymentService.isValidBet(bet)) {
            paymentService.setAdminAndNimofyBetProfit(bet);
            betTransactionRepository.batchUpdateUserBalanceBasedOnGameOutcome(filteredGameTransactions, bet);
        } else {
            betTransactionRepository.batchResetUserBalance(filteredGameTransactions);
        }
    }
}