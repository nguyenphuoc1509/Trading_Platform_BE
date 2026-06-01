package com.phuocnt.trading_platform_be.controller;

import com.phuocnt.trading_platform_be.entity.Order;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.service.OrderService;
import com.phuocnt.trading_platform_be.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @PostMapping("/buy")
    public ResponseEntity<Order> buy(@AuthenticationPrincipal Jwt jwt,
                                     @RequestParam String coinId,
                                     @RequestParam BigDecimal quantity) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(orderService.buyOrder(user, coinId, quantity));
    }

    @PostMapping("/sell")
    public ResponseEntity<Order> sell(@AuthenticationPrincipal Jwt jwt,
                                     @RequestParam String coinId,
                                     @RequestParam BigDecimal quantity) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(orderService.sellOrder(user, coinId, quantity));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(orderService.getOrdersByUser(user));
    }

}
