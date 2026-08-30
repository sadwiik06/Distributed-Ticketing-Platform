package com.sai.ticketing.ticketlock.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventCreatedEvent(
        @JsonAlias({"id", "eventId"})
        String eventId,
        @JsonAlias({"totalTickets", "totalSeats"})
        Integer totalSeats
) {}

