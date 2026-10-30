package com.example.demo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PlayRequest(
        @Pattern(regexp = ".*\\S.*") String title,
        @Positive Integer durationMinutes,
        @PositiveOrZero Integer ageRestriction) {
}
