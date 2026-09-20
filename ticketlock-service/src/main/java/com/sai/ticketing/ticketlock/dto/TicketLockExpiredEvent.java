package com.sai.ticketing.ticketlock.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TicketLockExpiredEvent(
        String eventId,
        String seatCode,
        Long timestamp
) {}
