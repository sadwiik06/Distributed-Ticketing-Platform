package com.sai.ticketing.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TicketLockedEvent {
    private String eventId;
    private String seatCode;
    private String userId;
    private Long timestamp;
}
