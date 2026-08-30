package com.sai.ticketing.event.service;

import com.sai.ticketing.event.dto.EventRequest;
import com.sai.ticketing.event.dto.EventResponse;
import com.sai.ticketing.event.model.Event;
import com.sai.ticketing.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository eventRepository;
    private final KafkaTemplate<String, Event> kafkaTemplate;


    public EventResponse createEvent(EventRequest eventRequest) {
        Event event = Event.builder()
                .name(eventRequest.name())
                .description(eventRequest.description())
                .venue(eventRequest.venue())
                .eventDate(eventRequest.eventDate())
                .ticketPrice(eventRequest.ticketPrice())
                .totalTickets(eventRequest.totalTickets())
                .availableTickets(eventRequest.totalTickets())
                .build();

        Event savedEvent = eventRepository.save(event);
        kafkaTemplate.send("event-created-topic", savedEvent.getId(), savedEvent);

        log.info("Event {} created successfully", savedEvent.getId());
        return mapToEventResponse(savedEvent);
    }

    public List<EventResponse> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return events.stream().map(this::mapToEventResponse).toList();
    }

    public EventResponse getEventById(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return mapToEventResponse(event);
    }

    private EventResponse mapToEventResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getVenue(),
                event.getEventDate(),
                event.getTicketPrice(),
                event.getTotalTickets()
        );
    }
}