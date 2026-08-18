package com.sai.ticketing.order.dto;
import java.math.BigDecimal;

public record OrderPlacedEvent(
        String orderNumber,
        String eventId,
        String userId,
        Integer quantity,
        BigDecimal totalPrice
) {}