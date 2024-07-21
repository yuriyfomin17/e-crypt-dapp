package com.nimofy.customerserver.service.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimofy.customerserver.service.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

import static com.nimofy.customerserver.service.cache.CacheService.ETH_USD_PRICE_CACHE_NAME;


@Service
@Slf4j
@RequiredArgsConstructor
public class Web3Service {

    private static final BigInteger GAS_LIMIT = Transfer.GAS_LIMIT.multiply(BigInteger.valueOf(40));
    private final RestTemplate restTemplate;
    private final Transfer transfer;
    private final Web3j web3j;

    @Value("${binance.rate.url}")
    private String BINANCE_URL;

    @Cacheable(cacheNames = ETH_USD_PRICE_CACHE_NAME, cacheManager = CacheService.CACHE_MANAGER_NAME)
    public Double getEthUsdRate() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return Objects.requireNonNull(restTemplate.exchange(BINANCE_URL, HttpMethod.GET, new HttpEntity<>("", httpHeaders), JsonNode.class)
                        .getBody())
                .get("price")
                .asDouble();
    }

    public Optional<String> createWithdrawTx(String address, BigDecimal ethAmount) {
        try {
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            BigInteger transactionFeeWei = gasPrice.multiply(GAS_LIMIT);
            BigDecimal totalTxAmount = Convert.toWei(ethAmount, Convert.Unit.ETHER);
            BigDecimal txAmount = totalTxAmount.subtract(new BigDecimal(transactionFeeWei));
            TransactionReceipt transactionReceipt = transfer.sendFunds(address, txAmount, Convert.Unit.WEI, gasPrice, GAS_LIMIT).send();
            return Optional.ofNullable(transactionReceipt.getTransactionHash());
        } catch (Exception e) {
            log.error("Could withdraw funds to address [{}] amount [{}]", address, ethAmount, e);
            return Optional.empty();
        }
    }

    public Transaction getTransactionByHash(String hash) {
        try {
            return web3j.ethGetTransactionByHash(hash).send().getTransaction().orElse(null);
        } catch (IOException e) {
            log.error("Could not find confirmations number for hash [{}]", hash);
            return null;
        }
    }

    public BigInteger getLatestBlockNumber() {
        try {
            return web3j.ethBlockNumber().send().getBlockNumber();
        } catch (IOException e) {
            log.error("Could not get latest block number");
            return BigInteger.ZERO;
        }
    }
}