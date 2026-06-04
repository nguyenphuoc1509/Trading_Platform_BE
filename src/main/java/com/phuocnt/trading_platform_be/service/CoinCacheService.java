package com.phuocnt.trading_platform_be.service;

import com.phuocnt.trading_platform_be.entity.Coin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
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

    // Coin list cache
    public Optional<List<Coin>> getCoinList(int page) {
        try {
            String json = redisTemplate.opsForValue().get(KEY_COIN_LIST + page);
            if (json == null) return Optional.empty();
            List<Coin> coins = objectMapper.readValue(json, new TypeReference<List<Coin>>() {});
            log.debug("[Cache] HIT coins:list:page.{}", page);
            return Optional.of(coins);
        } catch (Exception e) {
            log.warn("[Cache] Failed to get coins:list:page.{}: {}", page, e.getMessage());
            return Optional.empty();
        }
    }

    public void saveCoinList(int page, List<Coin> coins) {
        try {
            String json = objectMapper.writeValueAsString(coins);
            redisTemplate.opsForValue().set(KEY_COIN_LIST + page, json, COIN_LIST_TTL);
            log.debug("[Cache] SAVED coins:list:page:{} ({} coins, TTL 30s)", page, coins.size());
        } catch (Exception e) {
            log.warn("[Cache] Failed to save coins:list:page.{}: {}", page, e.getMessage());
        }
    }

    public void invalidateCoinList() {
        try {
            var keys = redisTemplate.keys(KEY_COIN_LIST + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("[Cache] Invalidated {} coin list keys ", keys.size());
            }
        } catch (Exception e) {
            log.warn("[Cache] Failed to invalidate coin list keys: {}", e.getMessage());
        }
    }

    // Lastest price cache ( from Binance Stream )
    public void savePrice(String coinId, String price) {
        try {
            redisTemplate.opsForValue().set(KEY_COIN_PRICE + coinId, price);
        } catch (Exception e) {
            log.warn("[Cache] Failed to save price for {}: {}", coinId, e.getMessage());
        }
    }

    public Optional<String> getPrice(String coinId) {
        try {
            String price = redisTemplate.opsForValue().get(KEY_COIN_PRICE + coinId);
            return Optional.ofNullable(price);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
