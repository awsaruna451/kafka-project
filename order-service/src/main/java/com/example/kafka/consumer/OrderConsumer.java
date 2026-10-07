package com.example.kafka.consumer;

import com.example.kafka.model.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrder(@Payload OrderEvent event,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset) throws InterruptedException {
        log.info("Received order {} (customer={}, amount={}) from partition {} offset {}",
                event.orderId(), event.customerId(), event.amount(), partition, offset);
        Thread.sleep(15000);

        // Make this idempotent: delivery is at-least-once, so the same order may arrive twice.
        // If this method throws, the error handler retries, then sends the record to the DLT.
        if (event.amount() < 0) {
            throw new IllegalArgumentException("Invalid amount for order " + event.orderId());
        }
    }

    @KafkaListener(topics = "orders-processed", groupId = "txn-order")
    public void onOrderProcessed(@Payload OrderEvent event,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset) throws InterruptedException {
        log.info("Received transaction order {} (customer={}, amount={}) from partition {} offset {}",
                event.orderId(), event.customerId(), event.amount(), partition, offset);

        // Make this idempotent: delivery is at-least-once, so the same order may arrive twice.
        // If this method throws, the error handler retries, then sends the record to the DLT.
        if (event.amount() < 100) {
            throw new IllegalArgumentException("Invalid amount for order " + event.orderId());
        }
    }

    @KafkaListener(topics = "orders.DLT", groupId = "order-service-dlt")
    public void onOrderDL(@Payload OrderEvent event,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received  order.dlt {} (customer={}, amount={}) from partition {} offset {}",
                event.orderId(), event.customerId(), event.amount(), partition, offset);

    }
}
