package com.nimofy.customerserver.scheduler;

import com.nimofy.customerserver.dto.crypto.UserTransactionDto;
import com.nimofy.customerserver.model.transaction.crypto.CryptoTransaction;
import com.nimofy.customerserver.model.transaction.crypto.State;
import com.nimofy.customerserver.model.transaction.crypto.Type;
import com.nimofy.customerserver.repository.transaction.crypto.CryptoTransactionRepository;
import com.nimofy.customerserver.repository.user.UserRepository;
import com.nimofy.customerserver.service.messaging.MessageService;
import com.nimofy.customerserver.service.payment.CryptoPaymentService;
import com.nimofy.customerserver.service.payment.CryptoTransactionService;
import com.nimofy.customerserver.service.payment.Web3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class CryptoTransactionSyncScheduler {

    @Value("${nimofy.user.id}")
    private Long NIMOFY_ID;
    @Value("${nimofy.wallet.address}")
    private String NIMOFY_WALLET_ADDRESS;

    private final CryptoPaymentService cryptoPaymentService;
    private final CryptoTransactionRepository cryptoTransactionRepository;
    private final Web3Service web3Service;
    private final CryptoTransactionService cryptoTransactionService;
    private final UserRepository userRepository;
    private final MessageService messageService;


    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void handlePendingDepositTxs() {
        List<CryptoTransaction> cryptoTransactions = cryptoTransactionRepository.fetchTxByStateInAndType(Set.of(State.PROCESSING, State.FAILING), Type.DEPOSIT);
        log.info("STARTED DEPOSIT TX SYNC - size [{}]", cryptoTransactions.size());
        cryptoTransactions.forEach(this::handleTx);
        log.info("FINISHED DEPOSIT TX SYNC - size [{}]", cryptoTransactions.size());

    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void handleProcessingWithdrawTxs() {
        List<CryptoTransaction> cryptoTransactions = cryptoTransactionRepository.fetchTxByStateInAndType(Set.of(State.PROCESSING, State.FAILING), Type.WITHDRAW);
        log.info("STARTED WITHDRAW TX SYNC - size [{}]", cryptoTransactions.size());
        cryptoTransactions.forEach(this::handleTx);
        log.info("FINISHED WITHDRAW TX SYNC - size [{}]", cryptoTransactions.size());
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.DAYS)
    public void sendNimofyPayout() {
        log.info("STARTED NIMOFY PAYOUT");
        log.info("FINISHED NIMOFY PAYOUT - response [{}]", cryptoPaymentService.createWithdrawTxUpdateCache(UserTransactionDto.builder()
                .address(NIMOFY_WALLET_ADDRESS)
                .userId(NIMOFY_ID)
                .ethAmount(userRepository.ethBalanceByUserId(NIMOFY_ID).toPlainString())
                .build()));
    }

    private void handleTx(CryptoTransaction tx) {
        Transaction transactionByHash = web3Service.getTransactionByHash(tx.getHash());
        if (transactionByHash != null) {
            BigInteger blockDifference = web3Service.getLatestBlockNumber().subtract(transactionByHash.getBlockNumber());
            CryptoTransaction cryptoTransaction = cryptoTransactionService.updateTx(tx, blockDifference.longValue(), Convert.fromWei(new BigDecimal(transactionByHash.getValue()), Convert.Unit.ETHER));
            if (cryptoTransaction.getType() == Type.WITHDRAW && cryptoTransaction.getState() == State.PROCESSED) {
                messageService.sendAccountInformationToUser(cryptoTransaction.getUser());
            }
            if (cryptoTransaction.getType() == Type.DEPOSIT && cryptoTransaction.getState() == State.PROCESSED) {
                messageService.sendAccountInformationToUser(cryptoTransaction.getUser());
            }
            log.info("SYNC - TxId [{}] state [{}] type [{}] confirmations [{}]", cryptoTransaction.getId(), cryptoTransaction.getState(), cryptoTransaction.getType(), cryptoTransaction.getConfirmations());
        }
    }
}