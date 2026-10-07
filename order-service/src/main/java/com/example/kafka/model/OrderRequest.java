package com.example.kafka.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record OrderRequest(@NotBlank String customerId, @Positive double amount) {}
