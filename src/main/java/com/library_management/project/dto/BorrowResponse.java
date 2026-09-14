package com.library_management.project.dto;

public record BorrowResponse(boolean success, Long loanId, String denialReason) {

    public static BorrowResponse ok(Long loanId) {
        return new BorrowResponse(true, loanId, null);
    }

    public static BorrowResponse denied(String denialReason) {
        return new BorrowResponse(false, null, denialReason);
    }
}
