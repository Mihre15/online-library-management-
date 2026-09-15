package com.library_management.library_project.exception;

public class AccountSuspendedException extends RuntimeException {
  public AccountSuspendedException(String message) {
    super(message);
  }
}
