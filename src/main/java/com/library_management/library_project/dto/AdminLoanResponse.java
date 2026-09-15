package com.library_management.library_project.dto;

import com.library_management.library_project.entity.Loan;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AdminLoanResponse(
    Long loanId,
    MemberSummary member,
    BookSummary book,
    LocalDate borrowDate,
    LocalDate dueDate,
    LocalDate returnDate,
    String status,
    BigDecimal fineAmount) {

  public record MemberSummary(Long id, String fullName, String email) {}

  public record BookSummary(Long id, String title, String author, String isbn) {}

  public static AdminLoanResponse from(Loan loan) {
    var member = loan.getMember();
    var book = loan.getBook();
    return new AdminLoanResponse(
        loan.getId(),
        new MemberSummary(member.getId(), member.getFullName(), member.getEmail()),
        new BookSummary(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn()),
        loan.getBorrowDate(),
        loan.getDueDate(),
        loan.getReturnDate(),
        loan.getStatus().name(),
        loan.getFineAmount());
  }
}
