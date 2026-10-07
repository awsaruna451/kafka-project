package com.example.kafka.model;

public record OrderEvent(String orderId, String customerId, double amount, String status) {}
