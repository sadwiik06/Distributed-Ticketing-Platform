package com.sai.ticketing.ticketlock.service;

import com.sai.ticketing.ticketlock.model.TicketInventory;
import com.sai.ticketing.ticketlock.repository.TicketInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketLockService {

    private final StringRedisTemplate redisTemplate;
    private final TicketInventoryRepository ticketInventoryRepository;

    @Transactional
    public boolean lockTicket(String eventId, String seatCode, String userId) {
        String lockKey = "lock:event:" + eventId + ":seat:" + seatCode;

        // Atomic Redis lock with 10-minute TTL
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, userId, Duration.ofMinutes(10));

        if (Boolean.TRUE.equals(acquired)) {
            Optional<TicketInventory> ticketOpt = ticketInventoryRepository.findByEventIdAndSeatCode(eventId, seatCode);
            if (ticketOpt.isPresent()) {
                TicketInventory ticket = ticketOpt.get();
                if ("AVAILABLE".equals(ticket.getStatus())) {
                    ticket.setStatus("LOCKED");
                    ticketInventoryRepository.save(ticket);
                    log.info("Seat {} locked successfully for user {}", seatCode, userId);
                    return true;
                }
            }
            // Roll back Redis lock if MySQL check fails
            redisTemplate.delete(lockKey);
        }
        log.warn("Seat {} is already locked or unavailable", seatCode);
        return false;
    }
}
