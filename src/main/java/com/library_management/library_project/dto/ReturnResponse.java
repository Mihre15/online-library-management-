package com.library_management.library_project.dto;

import java.math.BigDecimal;

public record ReturnResponse(
    boolean success,
    String status,
    BigDecimal fineCharged,
    String message) {
}
