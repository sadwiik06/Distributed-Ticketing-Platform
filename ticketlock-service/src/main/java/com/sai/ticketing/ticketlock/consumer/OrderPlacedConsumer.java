package com.sai.ticketing.ticketlock.consumer;

import com.sai.ticketing.ticketlock.dto.OrderPlacedEvent;
import com.sai.ticketing.ticketlock.repository.TicketInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderPlacedConsumer {

    private final TicketInventoryRepository ticketInventoryRepository;
    private final StringRedisTemplate redisTemplate;

    @KafkaListener(
            topics = "order-placed-topic",
            groupId = "ticket-lock-group",
            properties = {"spring.json.value.default.type=com.sai.ticketing.ticketlock.dto.OrderPlacedEvent"}
    )
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("TicketLockService received OrderPlacedEvent for seat: {}", event.seatCode());

        // Update DB: Mark seat as permanently booked
        ticketInventoryRepository.findByEventIdAndSeatCode(event.eventId(), event.seatCode())
                .ifPresent(ticket -> {
                    ticket.setStatus("CONFIRMED");
                    ticketInventoryRepository.save(ticket);
                    log.info("Seat {} status updated to CONFIRMED in DB", event.seatCode());
                });

        // Release temporary Redis lock
        String lockKey = "lock:event:" + event.eventId() + ":seat:" + event.seatCode();
        redisTemplate.delete(lockKey);
    }
}