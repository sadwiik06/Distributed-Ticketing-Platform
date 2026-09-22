package com.sai.ticketing.order.consumer;

import com.sai.ticketing.order.dto.TicketLockedEvent;
import com.sai.ticketing.order.model.Order;
import com.sai.ticketing.order.model.OrderStatus;
import com.sai.ticketing.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "ticket-lock-events",
            groupId = "order-group",
            properties = {"spring.json.value.default.type=com.sai.ticketing.order.dto.TicketLockedEvent"}
    )
    @Transactional
    public void handleTicketLockedEvent(TicketLockedEvent event) {
        log.info("Received ticket lock event for user: {}, seat: {}, event: {}",
                event.getUserId(), event.getSeatCode(), event.getEventId());

        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setUserId(event.getUserId());
        order.setEventId(event.getEventId());
        order.setSeatCode(event.getSeatCode());
        order.setQuantity(1);
        order.setAmount(new BigDecimal("150.00"));
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);
        log.info("Order created successfully with ID: {} for seat: {}", order.getOrderId(), order.getSeatCode());
    }

    @KafkaListener(
            topics = "ticket-lock-expired-topic",
            groupId = "order-group",
            properties = {"spring.json.value.default.type=com.sai.ticketing.order.dto.TicketLockExpiredEvent"}
    )
    @Transactional
    public void handleTicketLockExpired(com.sai.ticketing.order.dto.TicketLockExpiredEvent event) {
        log.info("Received ticket lock expired event for event: {}, seat: {}", event.getEventId(), event.getSeatCode());

        List<Order> pendingOrders = orderRepository.findByEventIdAndSeatCodeAndStatus(
                event.getEventId(),
                event.getSeatCode(),
                OrderStatus.PENDING
        );

        for (Order order : pendingOrders) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("Order {} status updated to CANCELLED due to lock timeout", order.getOrderId());
        }
    }
}
