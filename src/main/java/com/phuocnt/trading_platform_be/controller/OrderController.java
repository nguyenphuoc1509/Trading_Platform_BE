package com.phuocnt.trading_platform_be.controller;

import com.phuocnt.trading_platform_be.dto.request.OrderRequest;
import com.phuocnt.trading_platform_be.dto.response.ApiEnvelope;
import com.phuocnt.trading_platform_be.dto.response.OrderResponse;
import com.phuocnt.trading_platform_be.entity.Order;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.enums.OrderType;
import com.phuocnt.trading_platform_be.mapper.OrderMapper;
import com.phuocnt.trading_platform_be.service.OrderService;
import com.phuocnt.trading_platform_be.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    // POST /api/orders/buy
    // Body: { "coinId": "bitcoin", "quantity": 0.01, "mode": "MARKET" }
    // Body: { "coinId": "bitcoin", "quantity": 0.01, "mode": "LIMIT", "limitPrice": 60000 }
    @PostMapping("/buy")
    public ResponseEntity<ApiEnvelope<OrderResponse>> buy(@AuthenticationPrincipal Jwt jwt,
                                                          @Valid @RequestBody OrderRequest request) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(ApiEnvelope.success(OrderMapper.toOrderResponse(orderService.placeOrder(user, request, OrderType.BUY))));
    }

    // POST /api/orders/sell
    @PostMapping("/sell")
    public ResponseEntity<ApiEnvelope<OrderResponse>> sell(@AuthenticationPrincipal Jwt jwt,
                                      @Valid @RequestBody OrderRequest request) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(ApiEnvelope.success(OrderMapper.toOrderResponse(orderService.placeOrder(user, request, OrderType.SELL))));
    }

    // DELETE /api/orders/{orderId}/cancel
    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<ApiEnvelope<OrderResponse>> cancel(@AuthenticationPrincipal Jwt jwt,
                                        @PathVariable Long orderId) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(ApiEnvelope.success(OrderMapper.toOrderResponse(orderService.cancelOrder(user, orderId))));
    }

    // GET /api/orders — get history orders
    @GetMapping
    public ResponseEntity<ApiEnvelope<List<OrderResponse>>> getOrders(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(ApiEnvelope.success(OrderMapper.toOrderResponseList(orderService.getOrdersByUser(user))));
    }

    // GET /api/orders/pending — get orders PENDING
    @GetMapping("/pending")
    public ResponseEntity<ApiEnvelope<List<OrderResponse>>> getPending(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(ApiEnvelope.success(OrderMapper.toOrderResponseList(orderService.getPendingOrders(user))));
    }
}