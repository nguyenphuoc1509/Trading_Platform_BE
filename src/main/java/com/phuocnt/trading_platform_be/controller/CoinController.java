package com.phuocnt.trading_platform_be.controller;

import com.phuocnt.trading_platform_be.dto.response.ApiEnvelope;
import com.phuocnt.trading_platform_be.dto.response.CoinSummaryResponse;
import com.phuocnt.trading_platform_be.dto.response.KlineResponse;
import com.phuocnt.trading_platform_be.entity.Coin;
import com.phuocnt.trading_platform_be.mapper.CoinMapper;
import com.phuocnt.trading_platform_be.service.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/coins")
@RequiredArgsConstructor
public class CoinController {

    private final CoinService coinService;

    @GetMapping
    public ResponseEntity<ApiEnvelope<List<CoinSummaryResponse>>> getCoinsList(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(ApiEnvelope.success(coinService.getCoinsList(page).stream()
                .map(CoinMapper::toCoinSummary)
                .collect(Collectors.toList())));
    }

    @GetMapping("/{coinId}")
    public ResponseEntity<ApiEnvelope<String>> getCoinDetail(@PathVariable String coinId) {
        return ResponseEntity.ok(ApiEnvelope.success(coinService.getCoinDetail(coinId)));
    }

    @GetMapping("/{coinId}/chart")
    public ResponseEntity<ApiEnvelope<String>> getMarketChart(@PathVariable String coinId,
                                                 @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(ApiEnvelope.success(coinService.getMarketChart(coinId, days)));
    }

    @GetMapping("{coinId}/klines")
    public ResponseEntity<ApiEnvelope<List<KlineResponse>>> getKlines(@PathVariable String coinId,
                                                                      @RequestParam(defaultValue = "1") String interval,
                                                                      @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(ApiEnvelope.success(coinService.getKlines(coinId, interval, limit)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiEnvelope<String>> searchCoin(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiEnvelope.success(coinService.searchCoin(keyword)));
    }

    @GetMapping("/top50")
    public ResponseEntity<ApiEnvelope<String>> getTop50() {
        return ResponseEntity.ok(ApiEnvelope.success(coinService.getTop50CoinsByMarketCapRank()));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiEnvelope<String>> getTrending() {
        return ResponseEntity.ok(ApiEnvelope.success(coinService.getTrendingCoins()));
    }
}