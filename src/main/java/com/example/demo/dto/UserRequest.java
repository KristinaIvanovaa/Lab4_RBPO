package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UserRequest(
        @Pattern(regexp = ".*\\S.*") String name,
        @Pattern(regexp = ".*\\S.*") @Email String email,
        Boolean enabled) {
}
