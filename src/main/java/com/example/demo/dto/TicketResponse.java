package com.example.demo.dto;

import java.math.BigDecimal;
import com.example.demo.model.TicketStatus;

public record TicketResponse(
        Long id, Long performanceId, int seatNumber, Long userId, TicketStatus status, BigDecimal pricePaid) {
}
