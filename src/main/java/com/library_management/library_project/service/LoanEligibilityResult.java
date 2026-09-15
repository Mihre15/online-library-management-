package com.library_management.library_project.service;

import java.util.List;

public record LoanEligibilityResult(boolean allowed, List<String> reasons) {

    public static LoanEligibilityResult allow() {
        return new LoanEligibilityResult(true, List.of());
    }

    public static LoanEligibilityResult deny(List<String> reasons) {
        return new LoanEligibilityResult(false, reasons);
    }
}
