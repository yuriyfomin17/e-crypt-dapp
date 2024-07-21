package com.nimofy.customerserver.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.nimofy.customerserver.service.cache.CacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.Transfer;

import java.util.concurrent.TimeUnit;

import static com.nimofy.customerserver.service.cache.CacheService.ETH_USD_PRICE_CACHE_NAME;

@Configuration
public class CommonConfig {

    @Value("${moonbase.alpha.url}")
    private String MOONBASE_URL;
    @Value("${wallet.public.key}")
    private String WALLET_PUBLIC_KEY;
    @Value("${wallet.private.key}")
    private String WALLET_PRIVATE_KEY;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    public TaskExecutor cryptoTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(12);
        taskExecutor.setMaxPoolSize(12);
        taskExecutor.setThreadNamePrefix("E_CRYPT_MESSAGE_TASK_EXECUTOR-");
        return taskExecutor;
    }

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(MOONBASE_URL));
    }

    @Bean
    public FastRawTransactionManager fastRawTransactionManager() {
        return new FastRawTransactionManager(web3j(), Credentials.create(WALLET_PRIVATE_KEY, WALLET_PUBLIC_KEY));
    }

    @Bean
    public Transfer transfer() {
        return new Transfer(web3j(), fastRawTransactionManager());
    }

    @Bean(name = CacheService.CACHE_MANAGER_NAME)
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                ETH_USD_PRICE_CACHE_NAME
        );
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .initialCapacity(100)
                        .maximumSize(10000)
                        .expireAfterWrite(1, TimeUnit.MINUTES)
                        .recordStats()
        );
        return cacheManager;
    }
}