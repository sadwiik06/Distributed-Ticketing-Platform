package com.sai.ticketing.ticketlock.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TicketLockedEvent(
        String eventId,
        String seatCode,
        String userId,
        Long timestamp
) {}
