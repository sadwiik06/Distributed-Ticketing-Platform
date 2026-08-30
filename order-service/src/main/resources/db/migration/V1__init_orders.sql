CREATE TABLE t_orders (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          order_number VARCHAR(255) NOT NULL UNIQUE,
                          event_id VARCHAR(255) NOT NULL,
                          user_id VARCHAR(255) NOT NULL,
                          quantity INT NOT NULL,
                          total_price DECIMAL(10,2) NOT NULL,
                          status VARCHAR(50) NOT NULL, -- PENDING, CONFIRMED, FAILED
                          seat_code VARCHAR(100) NOT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);