package com.sai.ticketing.ticketlock.consumer;

import com.sai.ticketing.ticketlock.dto.EventCreatedEvent;
import com.sai.ticketing.ticketlock.model.TicketInventory;
import com.sai.ticketing.ticketlock.repository.TicketInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventCreatedConsumer {

    private final TicketInventoryRepository ticketInventoryRepository;

    @KafkaListener(
            topics = "event-created-topic",
            groupId = "ticket-lock-group",
            properties = {"spring.json.value.default.type=com.sai.ticketing.ticketlock.dto.EventCreatedEvent"}
    )
    @Transactional
    public void handleEventCreated(EventCreatedEvent event) {
        int seatsCount = (event.totalSeats() != null) ? event.totalSeats() : 0;
        log.info("Generating {} seats for Event ID: {}", seatsCount, event.eventId());

        List<TicketInventory> seats = new ArrayList<>();
        for (int i = 1; i <= seatsCount; i++) {
            seats.add(TicketInventory.builder()
                    .eventId(event.eventId())
                    .seatCode("A-" + i)
                    .status("AVAILABLE")
                    .build());
        }

        // Fast batch insert
        if (!seats.isEmpty()) {
            ticketInventoryRepository.saveAll(seats);
            log.info("Successfully generated {} seats in DB for event {}!", seatsCount, event.eventId());
        }
    }
}
