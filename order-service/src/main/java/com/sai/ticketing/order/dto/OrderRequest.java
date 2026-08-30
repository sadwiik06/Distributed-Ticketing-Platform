package com.sai.ticketing.order.dto;

import java.math.BigDecimal;

public record OrderRequest(
        String eventId,
        String userId,
        Integer quantity,
        BigDecimal pricePerTicket,
        String seatCode
) {}