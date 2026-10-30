package com.example.demo.dto;

import jakarta.validation.constraints.Positive;

public record TicketRequest(
        @Positive Long performanceId,
        @Positive Integer seatNumber,
        @Positive Long userId) {
}
