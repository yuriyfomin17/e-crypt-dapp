package com.nimofy.customerserver.service.payment;

import com.nimofy.customerserver.dto.crypto.UserTransactionDto;
import com.nimofy.customerserver.dto.response.ResponseDto;
import com.nimofy.customerserver.model.transaction.crypto.CryptoTransaction;
import com.nimofy.customerserver.model.transaction.crypto.State;
import com.nimofy.customerserver.model.transaction.crypto.Type;
import com.nimofy.customerserver.model.user.User;
import com.nimofy.customerserver.repository.transaction.crypto.CryptoTransactionRepository;
import com.nimofy.customerserver.repository.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.nimofy.customerserver.dto.response.ResponseMessage.DEPOSIT_TRANSACTION_CREATED;
import static com.nimofy.customerserver.dto.response.ResponseType.SUCCESS;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoTransactionService {

    public static final Long CONFIRMATIONS_THRESHOLD = 5L;

    private final CryptoTransactionRepository cryptoTransactionRepository;
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final BigDecimalCalculator bigDecimalCalculator;

    @Transactional
    public CryptoTransaction updateTx(CryptoTransaction transaction, Long confirmations, BigDecimal ethAmount){
        CryptoTransaction cryptoTransaction = lockTx(transaction);
        cryptoTransaction.setConfirmations(confirmations);
        cryptoTransaction.setEthAmount(ethAmount);
        if (transaction.getType() == Type.WITHDRAW && confirmations.compareTo(0L) > 0){
            cryptoTransaction.setState(State.PROCESSED);
            return cryptoTransaction;
        }
        if (confirmations.compareTo(CONFIRMATIONS_THRESHOLD) >= 0){
            cryptoTransaction.setState(State.PROCESSED);
            User user = cryptoTransaction.getUser();
            user.setEthBalance(bigDecimalCalculator.add(user.getEthBalance(), ethAmount));
        } else if (confirmations.compareTo(0L) < 0){
            cryptoTransaction.setState(State.FAILING);
        }
        return cryptoTransaction;
    }

    private CryptoTransaction lockTx(CryptoTransaction binanceTransaction) {
        return cryptoTransactionRepository
                .findByIdWithLock(binanceTransaction.getId())
                .orElseThrow(() -> new RuntimeException(String.format("Could not find transaction by id [%d]", binanceTransaction.getId())));
    }

    @Transactional
    public ResponseDto createDepositTx(UserTransactionDto userTransactionDto) {
        cryptoTransactionRepository.save(CryptoTransaction.builder()
                .hash(userTransactionDto.getHash().toLowerCase())
                .user(entityManager.getReference(User.class, userTransactionDto.getUserId()))
                .type(Type.DEPOSIT)
                .state(State.PROCESSING)
                .build());
        return ResponseDto.builder()
                .responseType(DEPOSIT_TRANSACTION_CREATED)
                .type(SUCCESS)
                .build();
    }

    @Transactional
    public void createWithdrawTx(String txHash, Long userId, BigDecimal amountToWithdraw) {
        log.info("Created withdraw transaction userId [{}] hash [{}] amount [{}]", userId, txHash, amountToWithdraw);
        cryptoTransactionRepository.save(CryptoTransaction.builder()
                .user(userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(String.format("Could not find user with Id [%d]", userId))))
                .hash(txHash)
                .type(Type.WITHDRAW)
                .state(State.PROCESSING)
                .ethAmount(amountToWithdraw)
                .build());
    }
}