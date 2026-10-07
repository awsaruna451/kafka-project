package com.example.kafka.web;

import com.example.kafka.model.OrderEvent;
import com.example.kafka.model.OrderRequest;
import com.example.kafka.producer.OrderProducer;
import com.example.kafka.producer.OrderTxnProducer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderProducer producer;
    private final OrderTxnProducer orderTxnProducer;

    public OrderController(OrderProducer producer, OrderTxnProducer orderTxnProducer) {
        this.producer = producer;
        this.orderTxnProducer = orderTxnProducer;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrderEvent create(@Valid @RequestBody OrderRequest request) {
        return producer.publish(request);
    }

    @PostMapping("/txn")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrderEvent createTxn(@Valid @RequestBody OrderRequest request) {
        return orderTxnProducer.publishTxn(request);
    }
}
