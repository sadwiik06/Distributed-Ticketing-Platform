package com.sai.ticketing.event.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventRequest(
        String name,
        String description,
        String venue,
        LocalDateTime eventDate,
        BigDecimal ticketPrice,
        Integer totalTickets
) {}