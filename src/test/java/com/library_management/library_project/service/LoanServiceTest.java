package com.library_management.library_project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.library_management.library_project.entity.Book;
import com.library_management.library_project.entity.Loan;
import com.library_management.library_project.entity.LoanStatus;
import com.library_management.library_project.entity.Member;
import com.library_management.library_project.entity.MembershipStatus;
import com.library_management.library_project.exception.InvalidLoanStateException;
import com.library_management.library_project.exception.LoanNotAllowedException;
import com.library_management.library_project.exception.NotFoundException;
import com.library_management.library_project.repository.BookRepository;
import com.library_management.library_project.repository.LoanRepository;
import com.library_management.library_project.repository.MemberRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the loan state machine in {@link LoanService}. All collaborators
 * (repositories, {@link FineCalculator}, {@link LoanEligibilityService}) are test
 * doubles (Mockito mocks/stubs), so these tests exercise LoanService in isolation.
 *
 * Valid transitions covered: ACTIVE-&gt;RETURN_PENDING, OVERDUE-&gt;RETURN_PENDING,
 * RETURN_PENDING-&gt;RETURNED, ACTIVE-&gt;OVERDUE, ACTIVE-&gt;LOST, OVERDUE-&gt;LOST.
 * Invalid transitions covered: RETURNED/LOST are terminal, confirmReturn rejects
 * anything other than RETURN_PENDING, OVERDUE cannot be re-marked overdue, and
 * ACTIVE cannot be marked overdue early.
 */
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);

    @Mock private LoanRepository loanRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private BookRepository bookRepository;
    @Mock private FineCalculator fineCalculator;
    @Mock private LoanEligibilityService eligibilityService;

    private LoanService loanService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        loanService = new LoanService(
                loanRepository, memberRepository, bookRepository, fineCalculator, eligibilityService, fixedClock);
        // save(...) returns whatever entity is passed in, mimicking a persistence round-trip.
        // lenient: not every test in this class reaches a save() call (e.g. the reject-path tests).
        lenient().when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Member member(MembershipStatus status) {
        return Member.builder()
                .id(1L)
                .fullName("Ada Lovelace")
                .email("ada@example.com")
                .status(status)
                .registeredAt(TODAY.minusYears(1))
                .outstandingFines(BigDecimal.ZERO)
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

    private Loan loan(LoanStatus status, LocalDate dueDate, Member m, Book b) {
        return Loan.builder()
                .id(10L)
                .member(m)
                .book(b)
                .borrowDate(dueDate.minusDays(14))
                .dueDate(dueDate)
                .status(status)
                .fineAmount(BigDecimal.ZERO)
                .build();
    }

    // ---- borrowBook ----------------------------------------------------

    @Test
    @DisplayName("borrowBook: eligible member creates an ACTIVE loan and decrements availability")
    void borrowBook_eligible_createsLoan() {
        Member member = member(MembershipStatus.ACTIVE);
        Book book = book(2);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        when(loanRepository.countByMemberIdAndStatusIn(anyLong(), any())).thenReturn(0L);
        when(eligibilityService.evaluate(member, book, 0L)).thenReturn(LoanEligibilityResult.allow());

        Loan result = loanService.borrowBook(1L, 2L);

        assertThat(result.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(result.getBorrowDate()).isEqualTo(TODAY);
        assertThat(result.getDueDate()).isEqualTo(TODAY.plusDays(LoanService.DEFAULT_LOAN_PERIOD_DAYS));
        assertThat(book.getAvailableCopies()).isEqualTo(1);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    @DisplayName("borrowBook: ineligible member is rejected and nothing is persisted")
    void borrowBook_notEligible_throws() {
        Member member = member(MembershipStatus.SUSPENDED);
        Book book = book(2);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        when(loanRepository.countByMemberIdAndStatusIn(anyLong(), any())).thenReturn(0L);
        when(eligibilityService.evaluate(member, book, 0L))
                .thenReturn(LoanEligibilityResult.deny(List.of("Member is suspended")));

        assertThatThrownBy(() -> loanService.borrowBook(1L, 2L))
                .isInstanceOf(LoanNotAllowedException.class)
                .hasMessageContaining("suspended");

        verify(loanRepository, never()).save(any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("borrowBook: unknown member raises NotFoundException")
    void borrowBook_memberMissing_throws() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.borrowBook(1L, 2L)).isInstanceOf(NotFoundException.class);
    }

    // ---- requestReturn / confirmReturn (two-step return) ------------------
    //
    // The single-step returnBook(loanId) was split into a student-initiated
    // requestReturn (ACTIVE/OVERDUE -> RETURN_PENDING, no fine/inventory change
    // yet) and an admin-only confirmReturn (RETURN_PENDING -> RETURNED, fine
    // calculated and inventory restocked here) to match the two-step return
    // flow the frontend and README expect.

    @Test
    @DisplayName("requestReturn: ACTIVE -> RETURN_PENDING, no fine or inventory change yet")
    void requestReturn_active_transitionsToReturnPending() {
        Member member = member(MembershipStatus.ACTIVE);
        Book book = book(0);
        Loan loan = loan(LoanStatus.ACTIVE, TODAY.minusDays(5), member, book);
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        Loan result = loanService.requestReturn(10L);

        assertThat(result.getStatus()).isEqualTo(LoanStatus.RETURN_PENDING);
        assertThat(result.getReturnDate()).isNull();
        assertThat(book.getAvailableCopies()).isEqualTo(0);
        assertThat(member.getOutstandingFines()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("requestReturn: OVERDUE -> RETURN_PENDING is a valid transition")
    void requestReturn_overdue_transitionsToReturnPending() {
        Loan loan = loan(LoanStatus.OVERDUE, TODAY.minusDays(20), member(MembershipStatus.ACTIVE), book(0));
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        Loan result = loanService.requestReturn(10L);

        assertThat(result.getStatus()).isEqualTo(LoanStatus.RETURN_PENDING);
    }

    @Test
    @DisplayName("requestReturn: RETURNED is a terminal state and cannot be returned again")
    void requestReturn_alreadyReturned_throws() {
        Loan loan = loan(LoanStatus.RETURNED, TODAY.minusDays(5), member(MembershipStatus.ACTIVE), book(1));
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.requestReturn(10L)).isInstanceOf(InvalidLoanStateException.class);
    }

    @Test
    @DisplayName("requestReturn: LOST is a terminal state and cannot be returned")
    void requestReturn_lost_throws() {
        Loan loan = loan(LoanStatus.LOST, TODAY.minusDays(5), member(MembershipStatus.ACTIVE), book(1));
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.requestReturn(10L)).isInstanceOf(InvalidLoanStateException.class);
    }

    @Test
    @DisplayName("confirmReturn: RETURN_PENDING -> RETURNED, fine comes from FineCalculator (stubbed collaborator)")
    void confirmReturn_pending_transitionsToReturnedWithFine() {
        Member member = member(MembershipStatus.ACTIVE);
        Book book = book(0);
        Loan loan = loan(LoanStatus.RETURN_PENDING, TODAY.minusDays(5), member, book);
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));
        when(fineCalculator.calculate(5)).thenReturn(new BigDecimal("2.50"));

        Loan result = loanService.confirmReturn(10L);

        assertThat(result.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(result.getReturnDate()).isEqualTo(TODAY);
        assertThat(result.getFineAmount()).isEqualByComparingTo("2.50");
        assertThat(book.getAvailableCopies()).isEqualTo(1);
        assertThat(member.getOutstandingFines()).isEqualByComparingTo("2.50");
    }

    @Test
    @DisplayName("confirmReturn: cannot confirm a loan that was never returned (still ACTIVE)")
    void confirmReturn_active_throws() {
        Loan loan = loan(LoanStatus.ACTIVE, TODAY.minusDays(5), member(MembershipStatus.ACTIVE), book(0));
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.confirmReturn(10L)).isInstanceOf(InvalidLoanStateException.class);
    }

    @Test
    @DisplayName("confirmReturn: RETURNED is terminal and cannot be confirmed again")
    void confirmReturn_alreadyReturned_throws() {
        Loan loan = loan(LoanStatus.RETURNED, TODAY.minusDays(5), member(MembershipStatus.ACTIVE), book(1));
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.confirmReturn(10L)).isInstanceOf(InvalidLoanStateException.class);
    }

    // ---- markOverdue -------------------------------------------------------

    @Test
    @DisplayName("markOverdue: ACTIVE past its due date -> OVERDUE")
    void markOverdue_pastDue_transitions() {
        Loan loan = loan(LoanStatus.ACTIVE, TODAY.minusDays(1), member(MembershipStatus.ACTIVE), book(0));
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        Loan result = loanService.markOverdue(10L);

        assertThat(result.getStatus()).isEqualTo(LoanStatus.OVERDUE);
    }

    @Test
    @DisplayName("markOverdue: ACTIVE but not yet due is rejected (boundary: due date == today)")
    void markOverdue_notYetDue_throws() {
        Loan loan = loan(LoanStatus.ACTIVE, TODAY, member(MembershipStatus.ACTIVE), book(0));
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.markOverdue(10L)).isInstanceOf(InvalidLoanStateException.class);
    }

    @Test
    @DisplayName("markOverdue: an already-OVERDUE loan cannot be marked overdue again")
    void markOverdue_alreadyOverdue_throws() {
        Loan loan = loan(LoanStatus.OVERDUE, TODAY.minusDays(1), member(MembershipStatus.ACTIVE), book(0));
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.markOverdue(10L)).isInstanceOf(InvalidLoanStateException.class);
    }

    // ---- reportLost --------------------------------------------------------

    @Test
    @DisplayName("reportLost: ACTIVE -> LOST decrements the book's total copies")
    void reportLost_active_transitionsAndShrinksInventory() {
        Book book = book(0);
        Loan loan = loan(LoanStatus.ACTIVE, TODAY.plusDays(5), member(MembershipStatus.ACTIVE), book);
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        Loan result = loanService.reportLost(10L);

        assertThat(result.getStatus()).isEqualTo(LoanStatus.LOST);
        assertThat(book.getTotalCopies()).isEqualTo(2);
    }

    @Test
    @DisplayName("reportLost: OVERDUE -> LOST is a valid transition")
    void reportLost_overdue_transitions() {
        Loan loan = loan(LoanStatus.OVERDUE, TODAY.minusDays(3), member(MembershipStatus.ACTIVE), book(0));
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        Loan result = loanService.reportLost(10L);

        assertThat(result.getStatus()).isEqualTo(LoanStatus.LOST);
    }

    @Test
    @DisplayName("reportLost: RETURNED is terminal and cannot be reported lost")
    void reportLost_returned_throws() {
        Loan loan = loan(LoanStatus.RETURNED, TODAY.minusDays(3), member(MembershipStatus.ACTIVE), book(1));
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.reportLost(10L)).isInstanceOf(InvalidLoanStateException.class);
    }
}
