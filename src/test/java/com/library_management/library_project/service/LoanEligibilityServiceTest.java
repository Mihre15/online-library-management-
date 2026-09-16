package com.library_management.library_project.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.library_management.library_project.entity.Book;
import com.library_management.library_project.entity.Member;
import com.library_management.library_project.entity.MembershipStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Decision-table tests for {@link LoanEligibilityService}. No collaborators to mock —
 * the service is a pure function of (member, book, currentActiveLoanCount) — so these
 * are plain JUnit/AssertJ tests, no Mockito needed.
 *
 * Table under test:
 * <pre>
 * Rule | Member ACTIVE | Under loan limit | Copy available | Fines OK | Outcome
 *  1   |      T        |        T         |       T        |    T     | ALLOW
 *  2   |      F        |        -         |       -        |    -     | DENY (suspended)
 *  3   |      T        |        F         |       -        |    -     | DENY (loan limit)
 *  4   |      T        |        T         |       F        |    -     | DENY (no copies)
 *  5   |      T        |        T         |       T        |    F     | DENY (fines too high)
 * </pre>
 * Plus boundary-value coverage on the two numeric conditions (loan count vs. the
 * 5-loan cap, and outstanding fines vs. the 10.00 limit).
 */
class LoanEligibilityServiceTest {

    private final LoanEligibilityService service = new LoanEligibilityService();

    private Member member(MembershipStatus status, String outstandingFines) {
        return Member.builder()
                .id(1L)
                .fullName("Ada Lovelace")
                .email("ada@example.com")
                .status(status)
                .registeredAt(LocalDate.now().minusYears(1))
                .outstandingFines(new BigDecimal(outstandingFines))
                .build();
    }

    private Book book(int availableCopies) {
        return Book.builder()
                .id(2L)
                .title("Clean Code")
                .author("Robert Martin")
                .isbn("isbn-1")
                .category("Software")
                .totalCopies(3)
                .availableCopies(availableCopies)
                .build();
    }

    // ---- Rule 1: all conditions hold -> ALLOW ------------------------------

    @Test
    @DisplayName("Rule 1: active member, under limit, copy available, fines OK -> ALLOW")
    void allConditionsHold_allowsLoan() {
        var result = service.evaluate(member(MembershipStatus.ACTIVE, "0.00"), book(1), 0L);

        assertThat(result.allowed()).isTrue();
        assertThat(result.reasons()).isEmpty();
    }

    // ---- Rule 2: suspended member -> DENY, independent of other conditions ----

    @Test
    @DisplayName("Rule 2: suspended member is denied even when every other condition holds")
    void suspendedMember_isDenied() {
        var result = service.evaluate(member(MembershipStatus.SUSPENDED, "0.00"), book(1), 0L);

        assertThat(result.allowed()).isFalse();
        assertThat(result.reasons()).containsExactly("Member is suspended");
    }

    // ---- Rule 3: at/over the loan limit -> DENY ----------------------------

    @Test
    @DisplayName("Rule 3: member at the maximum active-loan count is denied (boundary: count == limit)")
    void atLoanLimit_isDenied() {
        var result = service.evaluate(member(MembershipStatus.ACTIVE, "0.00"), book(1), 5L);

        assertThat(result.allowed()).isFalse();
        assertThat(result.reasons()).containsExactly("Member has reached the maximum of 5 active loans");
    }

    @Test
    @DisplayName("boundary: one below the loan limit is still allowed")
    void oneBelowLoanLimit_isAllowed() {
        var result = service.evaluate(member(MembershipStatus.ACTIVE, "0.00"), book(1), 4L);

        assertThat(result.allowed()).isTrue();
    }

    @Test
    @DisplayName("boundary: over the loan limit is denied")
    void overLoanLimit_isDenied() {
        var result = service.evaluate(member(MembershipStatus.ACTIVE, "0.00"), book(1), 6L);

        assertThat(result.allowed()).isFalse();
        assertThat(result.reasons()).containsExactly("Member has reached the maximum of 5 active loans");
    }

    // ---- Rule 4: no copies available -> DENY -------------------------------

    @Test
    @DisplayName("Rule 4: no copies available is denied (boundary: availableCopies == 0)")
    void noCopiesAvailable_isDenied() {
        var result = service.evaluate(member(MembershipStatus.ACTIVE, "0.00"), book(0), 0L);

        assertThat(result.allowed()).isFalse();
        assertThat(result.reasons()).containsExactly("No copies of this book are available");
    }

    @Test
    @DisplayName("boundary: exactly one copy available is allowed")
    void oneCopyAvailable_isAllowed() {
        var result = service.evaluate(member(MembershipStatus.ACTIVE, "0.00"), book(1), 0L);

        assertThat(result.allowed()).isTrue();
    }

    // ---- Rule 5: outstanding fines exceed the limit -> DENY ----------------

    @Test
    @DisplayName("boundary: fines exactly at the 10.00 limit are still allowed (limit is exclusive)")
    void finesAtLimit_isAllowed() {
        var result = service.evaluate(member(MembershipStatus.ACTIVE, "10.00"), book(1), 0L);

        assertThat(result.allowed()).isTrue();
    }

    @Test
    @DisplayName("Rule 5: fines one cent over the limit are denied (boundary: limit + 0.01)")
    void finesJustOverLimit_isDenied() {
        var result = service.evaluate(member(MembershipStatus.ACTIVE, "10.01"), book(1), 0L);

        assertThat(result.allowed()).isFalse();
        assertThat(result.reasons()).containsExactly("Member's outstanding fines exceed 10.00");
    }

    @Test
    @DisplayName("boundary: fines well over the limit are denied")
    void finesWellOverLimit_isDenied() {
        var result = service.evaluate(member(MembershipStatus.ACTIVE, "50.00"), book(1), 0L);

        assertThat(result.allowed()).isFalse();
        assertThat(result.reasons()).containsExactly("Member's outstanding fines exceed 10.00");
    }

    // ---- Multiple simultaneous failures -> every reason is reported --------

    @Test
    @DisplayName("multiple failing conditions are all reported, not just the first")
    void multipleFailures_reportsAllReasons() {
        var result = service.evaluate(member(MembershipStatus.SUSPENDED, "25.00"), book(0), 5L);

        assertThat(result.allowed()).isFalse();
        assertThat(result.reasons()).containsExactly(
                "Member is suspended",
                "Member has reached the maximum of 5 active loans",
                "No copies of this book are available",
                "Member's outstanding fines exceed 10.00"
        );
    }
}
