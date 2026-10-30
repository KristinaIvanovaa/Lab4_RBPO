package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import com.example.demo.model.PerformanceStatus;

public record PerformanceRequest(
        @Positive Long playId,
        @Positive Long hallId,
        LocalDateTime startsAt,
        @DecimalMin("0.0") BigDecimal basePrice,
        PerformanceStatus status) {
}
