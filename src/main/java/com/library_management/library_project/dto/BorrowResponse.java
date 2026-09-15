package com.library_management.library_project.dto;

public record BorrowResponse(
    boolean success,
    Long loanId,
    String denialReason) {

  public static BorrowResponse allowed(Long loanId) {
    return new BorrowResponse(true, loanId, null);
  }

  public static BorrowResponse denied(String reason) {
    return new BorrowResponse(false, null, reason);
  }
}
