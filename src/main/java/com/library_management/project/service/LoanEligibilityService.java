package com.library_management.project.service;

import com.library_management.project.entity.Book;
import com.library_management.project.entity.Member;
import com.library_management.project.entity.MembershipStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Decision table for whether a member may borrow a book. Rules combine four
 * conditions; the loan is allowed only when every condition holds:
 *
 * <pre>
 * Rule | Member ACTIVE | Under loan limit | Copy available | Fines OK | Outcome
 *  1   |      T        |        T         |       T        |    T     | ALLOW
 *  2   |      F        |        -         |       -        |    -     | DENY (suspended)
 *  3   |      T        |        F         |       -        |    -     | DENY (loan limit)
 *  4   |      T        |        T         |       F        |    -     | DENY (no copies)
 *  5   |      T        |        T         |       T        |    F     | DENY (fines too high)
 * </pre>
 */
@Component
public class LoanEligibilityService {

    static final int MAX_ACTIVE_LOANS = 5;
    static final BigDecimal FINE_LIMIT = new BigDecimal("10.00");

    public LoanEligibilityResult evaluate(Member member, Book book, long currentActiveLoanCount) {
        List<String> reasons = new ArrayList<>();

        if (member.getStatus() != MembershipStatus.ACTIVE) {
            reasons.add("Member is suspended");
        }
        if (currentActiveLoanCount >= MAX_ACTIVE_LOANS) {
            reasons.add("Member has reached the maximum of " + MAX_ACTIVE_LOANS + " active loans");
        }
        if (book.getAvailableCopies() <= 0) {
            reasons.add("No copies of this book are available");
        }
        if (member.getOutstandingFines().compareTo(FINE_LIMIT) > 0) {
            reasons.add("Member's outstanding fines exceed " + FINE_LIMIT);
        }

        return reasons.isEmpty() ? LoanEligibilityResult.allow() : LoanEligibilityResult.deny(reasons);
    }
}
