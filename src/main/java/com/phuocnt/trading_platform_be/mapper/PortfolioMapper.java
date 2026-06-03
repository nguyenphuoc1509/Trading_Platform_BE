package com.phuocnt.trading_platform_be.mapper;

import com.phuocnt.trading_platform_be.dto.response.PortfolioItemResponse;
import com.phuocnt.trading_platform_be.dto.response.PortfolioResponse;
import com.phuocnt.trading_platform_be.entity.Portfolio;
import com.phuocnt.trading_platform_be.entity.PortfolioItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import static com.phuocnt.trading_platform_be.mapper.MapperUtils.*;

public class PortfolioMapper {

    public static PortfolioItemResponse toPortfolioItemResponse(PortfolioItem item) {
        BigDecimal currentPrice = item.getCoin() != null && item.getCoin().getCurrentPrice() != null
                ? BigDecimal.valueOf(item.getCoin().getCurrentPrice())
                : BigDecimal.ZERO;

        BigDecimal quantity = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;

        BigDecimal avgBuyPrice = item.getAvgBuyPrice() != null ? item.getAvgBuyPrice() : BigDecimal.ZERO;

        BigDecimal currentValue = currentPrice.multiply(quantity).setScale(4, RoundingMode.HALF_UP);

        BigDecimal pnl = currentPrice.subtract(avgBuyPrice).multiply(quantity).setScale(4, RoundingMode.HALF_UP);

        BigDecimal pnlPercentage = BigDecimal.ZERO;
        BigDecimal costBasis = avgBuyPrice.multiply(quantity);
        if (costBasis.compareTo(BigDecimal.ZERO) > 0) {
            pnlPercentage = pnl.divide(costBasis, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return PortfolioItemResponse.builder()
                .itemId(item.getId())
                .coin(CoinMapper.toCoinSummary(item.getCoin()))
                .quantity(fmt8(quantity))
                .avgBuyPrice(fmt4(avgBuyPrice))
                .currentPrice(fmt4(currentPrice))
                .currentValue(fmt4(currentValue))
                .pnl(fmt4(pnl))
                .pnlPercentage(fmt2(pnlPercentage))
                .isProfitable(pnlPercentage.compareTo(BigDecimal.ZERO) > 0)
                .build();
    }

    public static PortfolioResponse toPortfolioResponse(Portfolio portfolio, List<PortfolioItem> items) {
        List<PortfolioItemResponse> itemResponses = items.stream()
                .map(PortfolioMapper::toPortfolioItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalPnl = itemResponses.stream()
                .map(i -> new BigDecimal(i.getPnl()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = items.stream()
                .map(i -> i.getAvgBuyPrice().multiply(i.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPnlPercentage = BigDecimal.ZERO;
        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            totalPnlPercentage = totalPnl.divide(totalCost, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalValue = portfolio.getTotalValue() != null
                ? portfolio.getTotalValue() : BigDecimal.ZERO;

        return PortfolioResponse.builder()
                .totalValue(fmt4(totalValue))
                .totalCost(fmt4(totalCost))
                .totalPnl(fmt4(totalPnl))
                .totalPnlPercentage(fmt2(totalPnlPercentage))
                .updatedAt(toMillis(portfolio.getUpdatedAt()))
                .items(itemResponses)
                .build();
    }
}
