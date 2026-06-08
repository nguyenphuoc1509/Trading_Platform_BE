package com.phuocnt.trading_platform_be.service;

import com.phuocnt.trading_platform_be.dto.response.KlineResponse;
import com.phuocnt.trading_platform_be.entity.Coin;

import java.util.List;

public interface CoinService {
    List<Coin> getCoinsList(int page);

    String getMarketChart(String coinId, int days);

    String getCoinDetail(String coinId);

    Coin findCoinById(String coinId);

    String searchCoin(String keyword);

    String getTop50CoinsByMarketCapRank();

    String getTrendingCoins();

    List<KlineResponse> getKlines(String coinId, String interval, int limit);
}
