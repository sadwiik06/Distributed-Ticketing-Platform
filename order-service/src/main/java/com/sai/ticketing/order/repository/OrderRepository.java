package com.sai.ticketing.order.repository;

import com.sai.ticketing.order.model.Order;
import com.sai.ticketing.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByOrderNumberAndUserId(String orderNumber, String userId);
    List<Order> findByEventIdAndSeatCodeAndStatus(String eventId, String seatCode, OrderStatus status);
}
