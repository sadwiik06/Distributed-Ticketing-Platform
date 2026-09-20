package com.sai.ticketing.ticketlock.service;

import com.sai.ticketing.ticketlock.dto.TicketLockedEvent;
import com.sai.ticketing.ticketlock.model.TicketInventory;
import com.sai.ticketing.ticketlock.repository.TicketInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public boolean lockSeat(String eventId, String seatCode, String userId) {
        String lockKey = "lock:event:" + eventId + ":seat:" + seatCode;

        // Check if ticket exists in database
        Optional<TicketInventory> ticketOpt = ticketInventoryRepository.findByEventIdAndSeatCode(eventId, seatCode);
        if (ticketOpt.isEmpty()) {
            log.warn("Seat {} not found for event {}", seatCode, eventId);
            return false;
        }

        TicketInventory ticket = ticketOpt.get();

        // If already permanently booked/confirmed, cannot lock
        if ("CONFIRMED".equalsIgnoreCase(ticket.getStatus())) {
            log.warn("Seat {} is already permanently confirmed/booked for event {}", seatCode, eventId);
            return false;
        }

        // Atomic Redis lock with 10-minute TTL
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, userId, Duration.ofMinutes(10));

        if (Boolean.TRUE.equals(acquired)) {
            ticket.setStatus("LOCKED");
            ticketInventoryRepository.save(ticket);
            log.info("Seat {} locked successfully for user {}", seatCode, userId);

            // Publish lock event to Kafka
            TicketLockedEvent event = new TicketLockedEvent(eventId, seatCode, userId, System.currentTimeMillis());
            kafkaTemplate.send("ticket-lock-events", seatCode, event);
            log.info("TicketLockedEvent published to Kafka for seat {}", seatCode);

            return true;
        }

        log.warn("Seat {} is already actively locked by another user in Redis", seatCode);
        return false;
    }

    @Transactional
    public boolean lockTicket(String eventId, String seatCode, String userId) {
        return lockSeat(eventId, seatCode, userId);
    }
}
