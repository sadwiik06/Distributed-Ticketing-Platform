package com.sai.ticketing.order.repository;

import com.sai.ticketing.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
