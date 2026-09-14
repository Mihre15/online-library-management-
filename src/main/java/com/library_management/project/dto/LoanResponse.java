package com.library_management.project.dto;

import com.library_management.project.entity.Loan;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanResponse(
        Long id,
        Long memberId,
        Long bookId,
        LocalDate borrowDate,
        LocalDate dueDate,
        LocalDate returnDate,
        String status,
        BigDecimal fineAmount) {

    public static LoanResponse from(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getMember().getId(),
                loan.getBook().getId(),
                loan.getBorrowDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.getStatus().name(),
                loan.getFineAmount());
    }
}
