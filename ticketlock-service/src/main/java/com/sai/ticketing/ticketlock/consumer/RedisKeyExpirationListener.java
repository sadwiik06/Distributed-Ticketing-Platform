package com.sai.ticketing.ticketlock.consumer;

import com.sai.ticketing.ticketlock.dto.TicketLockExpiredEvent;
import com.sai.ticketing.ticketlock.repository.TicketInventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private static final Pattern LOCK_KEY_PATTERN = Pattern.compile("^lock:event:(.+):seat:(.+)$");

    private final TicketInventoryRepository ticketInventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
                                      TicketInventoryRepository ticketInventoryRepository,
                                      KafkaTemplate<String, Object> kafkaTemplate) {
        super(listenerContainer);
        this.ticketInventoryRepository = ticketInventoryRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        Matcher matcher = LOCK_KEY_PATTERN.matcher(expiredKey);
        if (matcher.matches()) {
            String eventId = matcher.group(1);
            String seatCode = matcher.group(2);

            log.info("Seat lock expired for event: {}, seat: {}. Reverting to AVAILABLE.", eventId, seatCode);

            // Revert seat in MySQL to AVAILABLE if not CONFIRMED
            ticketInventoryRepository.findByEventIdAndSeatCode(eventId, seatCode)
                    .ifPresent(ticket -> {
                        if ("LOCKED".equalsIgnoreCase(ticket.getStatus())) {
                            ticket.setStatus("AVAILABLE");
                            ticketInventoryRepository.save(ticket);
                            log.info("Seat {} status reverted to AVAILABLE in DB.", seatCode);

                            // Publish expiration event to Kafka so order-service cancels the pending order
                            TicketLockExpiredEvent event = new TicketLockExpiredEvent(eventId, seatCode, System.currentTimeMillis());
                            kafkaTemplate.send("ticket-lock-expired-topic", seatCode, event);
                            log.info("Published TicketLockExpiredEvent to Kafka for seat: {}", seatCode);
                        }
                    });
        }
    }
}
