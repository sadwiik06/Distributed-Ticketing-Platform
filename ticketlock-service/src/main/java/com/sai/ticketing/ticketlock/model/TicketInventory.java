package com.sai.ticketing.ticketlock.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_ticket_inventory")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "seat_code", nullable = false)
    private String seatCode;

    @Column(name = "status", nullable = false)
    private String status;
}