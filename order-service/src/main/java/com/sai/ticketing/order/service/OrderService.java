package com.sai.ticketing.order.service;
import com.sai.ticketing.order.dto.OrderPlacedEvent;
import com.sai.ticketing.order.dto.OrderRequest;
import com.sai.ticketing.order.model.Order;
import com.sai.ticketing.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
                .status("PENDING")
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
                totalPrice
        );
        kafkaTemplate.send("notification-topic", event);
        log.info("OrderPlacedEvent published to Kafka for order {}", orderNumber);

        return orderNumber;
    }
}
