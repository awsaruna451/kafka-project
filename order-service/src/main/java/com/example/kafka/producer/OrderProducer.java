package com.example.kafka.producer;

import com.example.kafka.model.OrderEvent;
import com.example.kafka.model.OrderRequest;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final String topic;

    public OrderProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate,
                         @Value("${app.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTemplate.setAllowNonTransactional(true);
        this.topic = topic;
    }

    public OrderEvent publish(OrderRequest req) {
        OrderEvent event = new OrderEvent(UUID.randomUUID().toString(),
                req.customerId(), req.amount(), "CREATED");

        // key = customerId -> all events of one customer go to the same partition (ordered)
        kafkaTemplate.send(topic, event.customerId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Send failed for order {}", event.orderId(), ex);
                    } else {
                        var m = result.getRecordMetadata();
                        log.info("Sent order {} to {}-{} @ offset {}",
                                event.orderId(), m.topic(), m.partition(), m.offset());
                    }
                });
        return event;
    }

}
