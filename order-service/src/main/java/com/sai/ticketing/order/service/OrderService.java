package com.sai.ticketing.order.service;

import com.sai.ticketing.order.dto.OrderPlacedEvent;
import com.sai.ticketing.order.dto.OrderRequest;
import com.sai.ticketing.order.model.Order;
import com.sai.ticketing.order.model.OrderStatus;
import com.sai.ticketing.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Transactional
    public String placeOrder(OrderRequest orderRequest) {
        String orderNumber = UUID.randomUUID().toString();
        BigDecimal totalPrice = orderRequest.pricePerTicket().multiply(BigDecimal.valueOf(orderRequest.quantity()));

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .eventId(orderRequest.eventId())
                .userId(orderRequest.userId())
                .quantity(orderRequest.quantity())
                .totalPrice(totalPrice)
                .status(OrderStatus.PENDING)
                .seatCode(orderRequest.seatCode())
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);
        log.info("Order {} created with PENDING status", orderNumber);

        // Push Order Event to Kafka topic for async processing
        OrderPlacedEvent event = new OrderPlacedEvent(
                orderNumber,
                orderRequest.eventId(),
                orderRequest.userId(),
                orderRequest.quantity(),
                totalPrice,
                orderRequest.seatCode()
        );
        kafkaTemplate.send("order-placed-topic", event.eventId(), event);
        log.info("OrderPlacedEvent published to Kafka for order {}", orderNumber);

        return orderNumber;
    }

    @Transactional
    public boolean processPayment(String orderId, String userId) {
        String cleanOrderId = orderId != null ? orderId.trim() : null;
        String cleanUserId = userId != null ? userId.trim() : null;
        log.info("Processing payment for order: {} by user: {}", cleanOrderId, cleanUserId);

        Optional<Order> orderOpt = orderRepository.findByOrderNumber(cleanOrderId);
        if (orderOpt.isEmpty()) {
            log.warn("Order {} not found", cleanOrderId);
            return false;
        }

        Order order = orderOpt.get();

        // Verify order belongs to the requesting Keycloak user
        if (!order.getUserId().equals(cleanUserId)) {
            log.warn("User {} is not authorized to checkout order {}", cleanUserId, cleanOrderId);
            return false;
        }

        // Check if order was cancelled or expired
        if (OrderStatus.CANCELLED.equals(order.getStatus()) || OrderStatus.FAILED.equals(order.getStatus())) {
            log.warn("Order {} has expired or been cancelled and cannot be paid", cleanOrderId);
            return false;
        }

        // Check if order is already confirmed
        if (OrderStatus.CONFIRMED.equals(order.getStatus())) {
            log.info("Order {} is already confirmed", cleanOrderId);
            return true;
        }

        // Mock payment processing success -> Update Order Status to CONFIRMED
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        log.info("Payment successful. Order {} status updated to CONFIRMED", orderId);

        // Notify downstream services (e.g. ticketlock-service to permanently book seat & clear Redis lock)
        OrderPlacedEvent event = new OrderPlacedEvent(
                order.getOrderNumber(),
                order.getEventId(),
                order.getUserId(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getSeatCode()
        );
        kafkaTemplate.send("order-placed-topic", event.eventId(), event);
        log.info("OrderPlacedEvent published to Kafka for confirmed order {}", orderId);

        return true;
    }
}
