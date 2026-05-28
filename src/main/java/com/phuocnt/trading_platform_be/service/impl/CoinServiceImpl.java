package com.phuocnt.trading_platform_be.service.impl;

import com.phuocnt.trading_platform_be.entity.Coin;
import com.phuocnt.trading_platform_be.repository.CoinRepository;
import com.phuocnt.trading_platform_be.service.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoinServiceImpl implements CoinService {

    private final CoinRepository coinRepository;
    private final ObjectMapper objectMapper;

    @Value("${coingecko.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private String callApi(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("CoinGecko API error: " + e.getMessage());
        }
    }

    @Override
    public List<Coin> getCoinsList(int page) {
        String url = baseUrl + "/coins/markets" +
                "?vs_currency=usd" +
                "&order=market_cap_desc" +
                "&per_page=10" +
                "&page=" + page +
                "&sparkline=false";

        try {
            String body = callApi(url);
            return objectMapper.readValue(body, new TypeReference<List<Coin>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse coin list: " + e.getMessage());
        }
    }

    @Override
    public String getMarketChart(String coinId, int days) {
        String url = baseUrl + "/coins/" + coinId +
                "/market_chart?vs_currency=usd&days=" + days;
        return callApi(url);
    }

    @Override
    public String getCoinDetail(String coinId) {
        String url = baseUrl + "/coins/" + coinId +
                "?localization=false&tickers=false&market_data=true" +
                "&community_data=false&developer_data=false&sparkline=false";
        return callApi(url);
    }

    @Override
    public Coin findCoinById(String coinId) {
        return coinRepository.findById(coinId)
                .orElseThrow(() -> new RuntimeException("Coin not found: " + coinId));
    }

    @Override
    public String searchCoin(String keyword) {
        String url = baseUrl + "/search?query=" + keyword;
        return callApi(url);
    }

    @Override
    public String getTop50CoinsByMarketCapRank() {
        String url = baseUrl + "/coins/markets" +
                "?vs_currency=usd" +
                "&order=market_cap_desc" +
                "&per_page=50" +
                "&page=1" +
                "&sparkline=false";
        return callApi(url);
    }

    @Override
    public String getTrendingCoins() {
        String url = baseUrl + "/search/trending";
        return callApi(url);
    }
}