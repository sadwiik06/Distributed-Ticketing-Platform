package com.sai.ticketing.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "seat_code", nullable = false)
    private String seatCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public String getOrderId() {
        return orderNumber;
    }

    public void setOrderId(String orderId) {
        this.orderNumber = orderId;
    }

    public BigDecimal getAmount() {
        return totalPrice;
    }

    public void setAmount(BigDecimal amount) {
        this.totalPrice = amount;
    }
}
