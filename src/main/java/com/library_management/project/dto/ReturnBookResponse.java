package com.library_management.project.dto;

import java.math.BigDecimal;

public record ReturnBookResponse(boolean success, String status, BigDecimal fineCharged, String message) {

    public static ReturnBookResponse pending(Long loanId) {
        return new ReturnBookResponse(
                true,
                "RETURN_PENDING",
                BigDecimal.ZERO,
                "Return requested. Bring the book to the library desk for confirmation.");
    }
}
