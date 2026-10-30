package com.example.demo.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record RescheduleRequest(@NotNull LocalDateTime startsAt) {
}
