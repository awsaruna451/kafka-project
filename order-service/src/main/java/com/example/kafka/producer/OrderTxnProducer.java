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
public class OrderTxnProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderTxnProducer.class);

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final String topic;

    public OrderTxnProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate,
                            @Value("${app.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public OrderEvent publishTxn(OrderRequest req) {
        OrderEvent event = new OrderEvent(UUID.randomUUID().toString(),
                    req.customerId(), req.amount(), "PROCESSED");

        kafkaTemplate.executeInTransaction(ops -> {
            ops.send("orders-processed", event.customerId(), event);   // use ops, not kafkaTemplate
            return true;
        });

        log.info("Committed transaction for order {}", event.orderId());
        return event;
    }

}
