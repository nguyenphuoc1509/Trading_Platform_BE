package com.phuocnt.trading_platform_be.mapper;

import com.phuocnt.trading_platform_be.dto.response.OrderResponse;
import com.phuocnt.trading_platform_be.entity.Coin;
import com.phuocnt.trading_platform_be.entity.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.phuocnt.trading_platform_be.mapper.MapperUtils.*;

public class OrderMapper {

    public static OrderResponse toOrderResponse(Order order) {
        Coin coin = order.getCoin();

        String symbol = coin != null ? coin.getSymbol().toUpperCase() + "-USDT" : null;

        String totalValue = null;
        if (order.getExecutedPrice() != null
        && order.getExecutedPrice().compareTo(BigDecimal.ZERO) > 0
        && order.getQuantity() != null) {
            totalValue = fmt4(order.getExecutedPrice().multiply(order.getQuantity()));
        }
        return OrderResponse.builder()
                .orderId(String.valueOf(order.getId()))
                .symbol(symbol)
                .coinName(coin != null ? coin.getName() : null)
                .coinImage(coin != null ? coin.getImage() : null)
                .quantity(fmt8(order.getQuantity()))
                .price(fmt4(order.getPrice()))
                .executedPrice(order.getExecutedPrice() != null
                        ? fmt4(order.getExecutedPrice()) : null)
                .totalValue(totalValue)
                .side(order.getType())
                .orderType(order.getMode())
                .status(order.getStatus())
                .createdAt(toMillis(order.getCreatedAt()))
                .executedAt(toMillis(order.getExecutedAt()))
                .build();
    }

    public static List<OrderResponse> toOrderResponseList(List<Order> orders) {
        return orders.stream().map(OrderMapper::toOrderResponse).collect(Collectors.toList());
    }
}
