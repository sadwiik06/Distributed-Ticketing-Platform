package com.sai.ticketing.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderPlacedEvent(
        String orderNumber,
        String eventId,
        String userId,
        Integer quantity,
        BigDecimal totalPrice,
        String seatCode
) {}

