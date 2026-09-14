package com.library_management.project.exception;

public class LoanNotAllowedException extends RuntimeException {

    public LoanNotAllowedException(String message) {
        super(message);
    }
}
