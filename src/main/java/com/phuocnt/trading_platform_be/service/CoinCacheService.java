package com.phuocnt.trading_platform_be.service;

import com.phuocnt.trading_platform_be.entity.Coin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoinCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration COIN_LIST_TTL = Duration.ofSeconds(30);
    private static final String KEY_COIN_LIST = "coins:list:page.";
    private static final String KEY_COIN_PRICE = "coins:preice:";

    // circuit breaker
    private boolean redisAvailable = true;
    private Instant lastRedisErrorTime = Instant.EPOCH;
    private static final Duration REDIS_RETRY_INTERVAL = Duration.ofSeconds(30);

    private boolean isRedisAvailable() {
        if (redisAvailable) return true;

        if (Duration.between(lastRedisErrorTime, Instant.now()).compareTo(REDIS_RETRY_INTERVAL) > 0) {
            redisAvailable = true;
        }
        return redisAvailable;
    }

    private void markRedisDown(String operation, Exception e) {
        if (redisAvailable) {
            log.warn("[Cache] Redis unavailable — {} will be skipped until reconnect. Reason: {}",
                    operation, e.getMessage());
            redisAvailable = false;
            lastRedisErrorTime = Instant.now();
        }
    }

    // Coin list cache
    public Optional<List<Coin>> getCoinList(int page) {
        if (!isRedisAvailable()) return Optional.empty();
        try {
            String json = redisTemplate.opsForValue().get(KEY_COIN_LIST + page);
            if (json == null) return Optional.empty();
            List<Coin> coins = objectMapper.readValue(json, new TypeReference<List<Coin>>() {});
            log.debug("[Cache] HIT coins:list:page.{}", page);
            return Optional.of(coins);
        } catch (Exception e) {
            markRedisDown("getCoinList", e);
            return Optional.empty();
        }
    }

    public void saveCoinList(int page, List<Coin> coins) {
        if (!isRedisAvailable()) return;
        try {
            String json = objectMapper.writeValueAsString(coins);
            redisTemplate.opsForValue().set(KEY_COIN_LIST + page, json, COIN_LIST_TTL);
            log.debug("[Cache] SAVED coins:list:page.{} ({} coins, TTL 30s)", page, coins.size());
            redisAvailable = true;
        } catch (Exception e) {
            markRedisDown("saveCoinList", e);
        }
    }


    public void invalidateCoinList() {
        if (!isRedisAvailable()) return;
        try {
            var keys = redisTemplate.keys(KEY_COIN_LIST + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("[Cache] Invalidated {} coin list keys", keys.size());
            }
        } catch (Exception e) {
            markRedisDown("invalidateCoinList", e);
        }
    }

    // Lastest price cache ( from Binance Stream )
    public void savePrice(String coinId, String price) {
        if (!isRedisAvailable()) return;
        try {
            redisTemplate.opsForValue().set(KEY_COIN_PRICE + coinId, price);
            redisAvailable = true;
        } catch (Exception e) {
            markRedisDown("savePrice:" + coinId, e);
        }
    }

    public Optional<String> getPrice(String coinId) {
        if (!isRedisAvailable()) return Optional.empty();
        try {
            String price = redisTemplate.opsForValue().get(KEY_COIN_PRICE + coinId);
            return Optional.ofNullable(price);
        } catch (Exception e) {
            markRedisDown("getPrice", e);
            return Optional.empty();
        }
    }
}
