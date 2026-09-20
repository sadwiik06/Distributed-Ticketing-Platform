package com.sai.ticketing.order.controller;

import com.sai.ticketing.order.dto.OrderRequest;
import com.sai.ticketing.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String placeOrder(@RequestBody OrderRequest orderRequest) {
        return orderService.placeOrder(orderRequest);
    }

    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(
            @RequestParam String orderId,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();

        // Process payment and verify order belongs to the requesting Keycloak user
        boolean success = orderService.processPayment(orderId, userId);

        if (success) {
            return ResponseEntity.ok("Payment confirmed! Ticket reserved.");
        } else {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Payment failed.");
        }
    }
}