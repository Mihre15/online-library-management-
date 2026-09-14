package com.library_management.project.service;

import java.util.List;

/**
 * Maps internal eligibility messages onto the four spec denial reasons (Section 8.3).
 * Order when several conditions fail: suspended, fine, limit, copies.
 */
public final class DenialReasonMapper {

    public static final String ACCOUNT_SUSPENDED = "Account suspended";
    public static final String FINE_TOO_HIGH = "Outstanding fine too high";
    public static final String BORROW_LIMIT = "Borrow limit reached";
    public static final String NO_COPIES = "No copies available";

    private DenialReasonMapper() {}

    public static String toSpecReason(String message) {
        if (message == null || message.isBlank()) {
            return NO_COPIES;
        }
        String lower = message.toLowerCase();
        if (lower.contains("suspended")) {
            return ACCOUNT_SUSPENDED;
        }
        if (lower.contains("fine")) {
            return FINE_TOO_HIGH;
        }
        if (lower.contains("maximum") || lower.contains("limit")) {
            return BORROW_LIMIT;
        }
        if (lower.contains("no copies") || lower.contains("available")) {
            return NO_COPIES;
        }
        return NO_COPIES;
    }

    public static String toSpecReason(List<String> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return NO_COPIES;
        }
        return toSpecReason(String.join("; ", reasons));
    }
}
