package com.phuocnt.trading_platform_be.mapper;

import com.phuocnt.trading_platform_be.dto.response.CoinSummaryResponse;
import com.phuocnt.trading_platform_be.entity.Coin;

import static com.phuocnt.trading_platform_be.mapper.MapperUtils.*;

public class CoinMapper {

    public static CoinSummaryResponse toCoinSummary(Coin coin) {
        if (coin == null) return null;

        return CoinSummaryResponse.builder()
                .id(coin.getId())
                .symbol(coin.getSymbol() != null ? coin.getSymbol().toUpperCase() : null)
                .name(coin.getName())
                .image(coin.getImage())
                .currentPrice(fmtDouble8(coin.getCurrentPrice()))
                .priceChange24h(fmtDouble4(coin.getPriceChangePercentage24h()))
                .marketCapRank(coin.getMarketCapRank())
                .build();
    }
}
