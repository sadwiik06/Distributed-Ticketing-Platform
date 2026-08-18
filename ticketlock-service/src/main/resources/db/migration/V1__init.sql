CREATE TABLE t_ticket_inventory (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    event_id VARCHAR(255) NOT NULL,
                                    seat_code VARCHAR(100) NOT NULL,
                                    status VARCHAR(50) NOT NULL, -- AVAILABLE, LOCKED, RESERVED
                                    UNIQUE KEY uq_event_seat (event_id, seat_code)
);

INSERT INTO t_ticket_inventory (event_id, seat_code, status) VALUES
                                                                 ('E101', 'A-1', 'AVAILABLE'),
                                                                 ('E101', 'A-2', 'AVAILABLE'),
                                                                 ('E101', 'A-3', 'AVAILABLE');