package com.sai.ticketing.event.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(value = "events")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Event {
    @Id
    private String id;
    private String name;
    private String description;
    private String venue;
    private LocalDateTime eventDate;
    private BigDecimal ticketPrice;
    private Integer totalTickets;
}