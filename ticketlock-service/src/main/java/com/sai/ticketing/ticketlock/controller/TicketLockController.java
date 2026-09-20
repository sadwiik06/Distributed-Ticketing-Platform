package com.sai.ticketing.ticketlock.controller;

import com.sai.ticketing.ticketlock.service.TicketLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lock")
@RequiredArgsConstructor
public class TicketLockController {

    private final TicketLockService ticketLockService;

    @PostMapping
    public ResponseEntity<String> lockSeat(
            @RequestParam String eventId,
            @RequestParam String seatCode,
            @AuthenticationPrincipal Jwt jwt) {

        // Extract immutable Keycloak User UUID from JWT 'sub' claim
        String userId = jwt.getSubject();

        boolean locked = ticketLockService.lockSeat(eventId, seatCode, userId);

        if (locked) {
            return ResponseEntity.ok("Seat locked successfully for user: " + userId);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Seat is already locked or unavailable.");
        }
    }
}