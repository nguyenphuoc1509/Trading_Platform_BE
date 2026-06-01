package com.phuocnt.trading_platform_be.controller;

import com.phuocnt.trading_platform_be.entity.Coin;
import com.phuocnt.trading_platform_be.service.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coins")
@RequiredArgsConstructor
public class CoinController {

    private final CoinService coinService;

    @GetMapping
    public ResponseEntity<List<Coin>> getCoinsList(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(coinService.getCoinsList(page));
    }

    @GetMapping("/{coinId}")
    public ResponseEntity<String> getCoinDetail(@PathVariable String coinId) {
        return ResponseEntity.ok(coinService.getCoinDetail(coinId));
    }

    @GetMapping("/{coinId}/chart")
    public ResponseEntity<String> getMarketChart(@PathVariable String coinId,
                                                 @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(coinService.getMarketChart(coinId, days));
    }

    @GetMapping("/search")
    public ResponseEntity<String> searchCoin(@RequestParam String keyword) {
        return ResponseEntity.ok(coinService.searchCoin(keyword));
    }

    @GetMapping("/top50")
    public ResponseEntity<String> getTop50() {
        return ResponseEntity.ok(coinService.getTop50CoinsByMarketCapRank());
    }

    @GetMapping("/trending")
    public ResponseEntity<String> getTrending() {
        return ResponseEntity.ok(coinService.getTrendingCoins());
    }
}