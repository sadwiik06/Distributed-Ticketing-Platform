package com.sai.ticketing.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @PostMapping("/ticket-lock")
    public Mono<ResponseEntity<String>> ticketLockFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Ticket Lock Service is currently overloaded or undergoing maintenance. Please try again in a few seconds."));
    }

    @PostMapping("/orders")
    public Mono<ResponseEntity<String>> orderFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Order Processing Service is currently unavailable. Your payment was NOT charged. Please retry shortly."));
    }
}