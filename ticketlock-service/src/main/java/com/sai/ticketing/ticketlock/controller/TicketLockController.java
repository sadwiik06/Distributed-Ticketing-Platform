package com.sai.ticketing.ticketlock.controller;

import com.sai.ticketing.ticketlock.service.TicketLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> lockTicket(@RequestParam String eventId,
                                             @RequestParam String seatCode,
                                             @RequestParam String userId) {
        boolean isLocked = ticketLockService.lockTicket(eventId, seatCode, userId);
        if (isLocked) {
            return ResponseEntity.ok("Seat locked successfully for 10 minutes.");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Seat unavailable or already locked by another user.");
        }
    }
}