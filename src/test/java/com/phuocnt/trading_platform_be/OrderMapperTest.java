package com.phuocnt.trading_platform_be;

import com.phuocnt.trading_platform_be.dto.response.OrderResponse;
import com.phuocnt.trading_platform_be.entity.Coin;
import com.phuocnt.trading_platform_be.entity.Order;
import com.phuocnt.trading_platform_be.enums.OrderMode;
import com.phuocnt.trading_platform_be.enums.OrderStatus;
import com.phuocnt.trading_platform_be.enums.OrderType;
import com.phuocnt.trading_platform_be.mapper.OrderMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    @Test
    void pendingLimitBuy_usesLimitPriceForTotalValueAndHidesExecutedPrice() {
        Coin coin = new Coin();
        coin.setSymbol("btc");
        coin.setName("Bitcoin");

        Order order = new Order();
        order.setId(1L);
        order.setCoin(coin);
        order.setQuantity(new BigDecimal("0.001"));
        order.setPrice(new BigDecimal("1.0000"));
        order.setExecutedPrice(BigDecimal.ZERO);
        order.setType(OrderType.BUY);
        order.setMode(OrderMode.LIMIT);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        OrderResponse response = OrderMapper.toOrderResponse(order);

        assertThat(response.getPrice()).isEqualTo("1.0000");
        assertThat(response.getExecutedPrice()).isNull();
        assertThat(response.getTotalValue()).isEqualTo("0.0010");
    }
}
