package com.sai.ticketing.order.consumer;

import com.sai.ticketing.order.dto.OrderPlacedEvent;
import com.sai.ticketing.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderPlacedConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "order-placed-topic",
            groupId = "order-service-group",
            properties = {"spring.json.value.default.type=com.sai.ticketing.order.dto.OrderPlacedEvent"}
    )
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("OrderService received OrderPlacedEvent for order: {}", event.orderNumber());

        orderRepository.findByOrderNumber(event.orderNumber())
                .ifPresent(order -> {
                    order.setStatus("CONFIRMED");
                    orderRepository.save(order);
                    log.info("Order {} status updated to CONFIRMED in DB", event.orderNumber());
                });
    }
}
