package com.example.demo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record TheatreHallRequest(@Pattern(regexp = ".*\\S.*") String name, @Positive Integer seatCount) {
}
