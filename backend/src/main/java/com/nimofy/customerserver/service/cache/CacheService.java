package com.nimofy.customerserver.service.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheService {
    public static final String CACHE_MANAGER_NAME = "E_CRYPT_CACHE_MANAGER";
    public static final String ETH_USD_PRICE_CACHE_NAME = "ETH_USD_PRICE_CACHE_NAME";
}