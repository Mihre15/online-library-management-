package com.library_management.project.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.library_management.project.entity.Book;
import com.library_management.project.entity.Member;
import com.library_management.project.entity.MembershipStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Decision table coverage for {@link LoanEligibilityService}. Conditions:
 *   C1 member ACTIVE, C2 under loan limit, C3 copy available, C4 fines within limit.
 * Every rule from the table in LoanEligibilityService's javadoc is exercised below,
 * plus the boundaries of the numeric conditions (loan count, fine amount).
 */
class LoanEligibilityServiceTest {

    private final LoanEligibilityService service = new LoanEligibilityService();

    private Member activeMember() {
        return Member.builder()
                .id(1L)
                .fullName("Ada Lovelace")
                .email("ada@example.com")
                .status(MembershipStatus.ACTIVE)
                .registeredAt(LocalDate.now())
                .outstandingFines(BigDecimal.ZERO)
                .build();
    }

    private Book availableBook() {
        return Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert Martin")
                .isbn("isbn-1")
                .category("Software")
                .totalCopies(3)
                .availableCopies(1)
                .build();
    }

    @Test
    @DisplayName("Rule 1: all conditions true -> allow")
    void allConditionsTrue_allows() {
        LoanEligibilityResult result = service.evaluate(activeMember(), availableBook(), 0);

        assertThat(result.allowed()).isTrue();
        assertThat(result.reasons()).isEmpty();
    }

    @Test
    @DisplayName("Rule 2: suspended member -> deny regardless of other conditions")
    void suspendedMember_denies() {
        Member suspended = activeMember();
        suspended.setStatus(MembershipStatus.SUSPENDED);

        LoanEligibilityResult result = service.evaluate(suspended, availableBook(), 0);

        assertThat(result.allowed()).isFalse();
        assertThat(result.reasons()).anyMatch(r -> r.contains("suspended"));
    }

    @Test
    @DisplayName("Rule 3: at the active-loan limit -> deny")
    void atLoanLimit_denies() {
        LoanEligibilityResult result =
                service.evaluate(activeMember(), availableBook(), LoanEligibilityService.MAX_ACTIVE_LOANS);

        assertThat(result.allowed()).isFalse();
        assertThat(result.reasons()).anyMatch(r -> r.contains("maximum"));
    }

    @Test
    @DisplayName("Boundary: one below the active-loan limit -> allow")
    void oneBelowLoanLimit_allows() {
        LoanEligibilityResult result =
                service.evaluate(activeMember(), availableBook(), LoanEligibilityService.MAX_ACTIVE_LOANS - 1);

        assertThat(result.allowed()).isTrue();
    }

    @Test
    @DisplayName("Rule 4: no copies available -> deny")
    void noCopiesAvailable_denies() {
        Book outOfStock = availableBook();
        outOfStock.setAvailableCopies(0);

        LoanEligibilityResult result = service.evaluate(activeMember(), outOfStock, 0);

        assertThat(result.allowed()).isFalse();
        assertThat(result.reasons()).anyMatch(r -> r.contains("No copies"));
    }

    @Test
    @DisplayName("Rule 5: fines at the limit -> allow (boundary is inclusive)")
    void finesAtLimit_allows() {
        Member member = activeMember();
        member.setOutstandingFines(LoanEligibilityService.FINE_LIMIT);

        LoanEligibilityResult result = service.evaluate(member, availableBook(), 0);

        assertThat(result.allowed()).isTrue();
    }

    @Test
    @DisplayName("Rule 5: fines just over the limit -> deny")
    void finesOverLimit_denies() {
        Member member = activeMember();
        member.setOutstandingFines(LoanEligibilityService.FINE_LIMIT.add(new BigDecimal("0.01")));

        LoanEligibilityResult result = service.evaluate(member, availableBook(), 0);

        assertThat(result.allowed()).isFalse();
        assertThat(result.reasons()).anyMatch(r -> r.contains("fines"));
    }

    @Test
    @DisplayName("Multiple failing conditions are all reported")
    void multipleFailures_areAllReported() {
        Member suspended = activeMember();
        suspended.setStatus(MembershipStatus.SUSPENDED);
        Book outOfStock = availableBook();
        outOfStock.setAvailableCopies(0);

        LoanEligibilityResult result = service.evaluate(suspended, outOfStock, 0);

        assertThat(result.allowed()).isFalse();
        assertThat(result.reasons()).hasSize(2);
    }
}
