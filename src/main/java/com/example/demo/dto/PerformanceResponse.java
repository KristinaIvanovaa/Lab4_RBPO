package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.demo.model.PerformanceStatus;

public record PerformanceResponse(Long id, Long playId, Long hallId, LocalDateTime startsAt,
                                  BigDecimal basePrice, PerformanceStatus status) {
}
