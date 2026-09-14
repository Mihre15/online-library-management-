package com.library_management.project.dto;

import com.library_management.project.entity.Book;
import com.library_management.project.entity.Loan;
import java.math.BigDecimal;
import java.time.LocalDate;

public record StudentLoanResponse(
        Long loanId,
        BookSummary book,
        String status,
        LocalDate dueDate,
        BigDecimal fineCharged) {

    public record BookSummary(Long id, String title, String author, String isbn) {}

    public static StudentLoanResponse from(Loan loan) {
        Book book = loan.getBook();
        return new StudentLoanResponse(
                loan.getId(),
                new BookSummary(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn()),
                loan.getStatus().name(),
                loan.getDueDate(),
                loan.getFineAmount());
    }
}
