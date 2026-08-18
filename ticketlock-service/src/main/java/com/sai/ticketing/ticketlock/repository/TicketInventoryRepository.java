package com.sai.ticketing.ticketlock.repository;

import com.sai.ticketing.ticketlock.model.TicketInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketInventoryRepository extends JpaRepository<TicketInventory,Long> {
    Optional<TicketInventory> findByEventIdAndSeatCode(String eventId, String seatCode);
}
