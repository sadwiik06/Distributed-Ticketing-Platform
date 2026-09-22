package com.sai.ticketing.event.consumer;
import com.sai.ticketing.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sai.ticketing.event.dto.OrderPlacedEvent;
@Service
@Slf4j
@RequiredArgsConstructor
public class EventInventoryConsumer {

    private final EventRepository eventRepository;

    @KafkaListener(topics = "order-placed-topic", groupId = "event-service-group")
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("EventService received OrderPlacedEvent for Event ID: {}", event.eventId());

        // Decrement available ticket count
        eventRepository.findById(event.eventId()).ifPresent(eventEntity -> {
            eventEntity.setAvailableTickets(eventEntity.getAvailableTickets() - 1);
            eventRepository.save(eventEntity);
            log.info("Decremented tickets for Event {}. Remaining: {}",
                    event.eventId(), eventEntity.getAvailableTickets());
        });
    }
}