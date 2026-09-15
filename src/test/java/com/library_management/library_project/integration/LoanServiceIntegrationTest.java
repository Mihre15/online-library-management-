package com.library_management.library_project.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.library_management.library_project.entity.Book;
import com.library_management.library_project.entity.Loan;
import com.library_management.library_project.entity.LoanStatus;
import com.library_management.library_project.entity.Member;
import com.library_management.library_project.entity.MembershipStatus;
import com.library_management.library_project.exception.LoanNotAllowedException;
import com.library_management.library_project.repository.BookRepository;
import com.library_management.library_project.repository.LoanRepository;
import com.library_management.library_project.repository.MemberRepository;
import com.library_management.library_project.service.LoanService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for the borrow/return flow: LoanService wired to real
 * MemberRepository/BookRepository/LoanRepository backed by an in-memory H2
 * database, exercising the whole persistence + business-logic stack together.
 *
 * Returning is two-step: requestReturn (student-initiated, -> RETURN_PENDING,
 * no fine/inventory change) then confirmReturn (admin-only, -> RETURNED, fine
 * calculated and inventory restocked here).
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(FixedClockTestConfig.class)
@Transactional
class LoanServiceIntegrationTest {

    static final LocalDate FIXED_TODAY = FixedClockTestConfig.FIXED_TODAY;

    @Autowired private LoanService loanService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private LoanRepository loanRepository;

    private Member member;
    private Book book;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .fullName("Ada Lovelace")
                .email("ada@example.com")
                .status(MembershipStatus.ACTIVE)
                .registeredAt(FIXED_TODAY.minusYears(1))
                .outstandingFines(BigDecimal.ZERO)
                .build());
        book = bookRepository.save(Book.builder()
                .title("Clean Code")
                .author("Robert Martin")
                .isbn("isbn-1")
                .category("Software")
                .totalCopies(1)
                .availableCopies(1)
                .build());
    }

    @Test
    void borrowThenReturnOnTime_noFineIsCharged() {
        Loan loan = loanService.borrowBook(member.getId(), book.getId());

        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableCopies()).isZero();

        loanService.requestReturn(loan.getId());
        Loan returned = loanService.confirmReturn(loan.getId());

        assertThat(returned.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(returned.getFineAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableCopies()).isEqualTo(1);
        assertThat(memberRepository.findById(member.getId()).orElseThrow().getOutstandingFines())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void returningLate_chargesAFineAndUpdatesMemberBalance() {
        Loan loan = loanRepository.save(Loan.builder()
                .member(member)
                .book(book)
                .borrowDate(FIXED_TODAY.minusDays(30))
                .dueDate(FIXED_TODAY.minusDays(10))
                .status(LoanStatus.ACTIVE)
                .fineAmount(BigDecimal.ZERO)
                .build());

        loanService.requestReturn(loan.getId());
        Loan returned = loanService.confirmReturn(loan.getId());

        // 10 days overdue, in the 8-30 day band at 1.00/day.
        assertThat(returned.getFineAmount()).isEqualByComparingTo("10.00");
        assertThat(memberRepository.findById(member.getId()).orElseThrow().getOutstandingFines())
                .isEqualByComparingTo("10.00");
    }

    @Test
    void borrow_deniedWhenNoCopiesAvailable_andNothingIsPersisted() {
        book.setAvailableCopies(0);
        bookRepository.saveAndFlush(book);

        assertThatThrownBy(() -> loanService.borrowBook(member.getId(), book.getId()))
                .isInstanceOf(LoanNotAllowedException.class);

        assertThat(loanRepository.findByMemberId(member.getId())).isEmpty();
    }

    @Test
    void borrow_deniedForSuspendedMember() {
        member.setStatus(MembershipStatus.SUSPENDED);
        memberRepository.saveAndFlush(member);

        assertThatThrownBy(() -> loanService.borrowBook(member.getId(), book.getId()))
                .isInstanceOf(LoanNotAllowedException.class)
                .hasMessageContaining("suspended");
    }

    @Test
    void fullLifecycle_markOverdueThenReturn_appliesHighBandFine() {
        Loan loan = loanRepository.save(Loan.builder()
                .member(member)
                .book(book)
                .borrowDate(FIXED_TODAY.minusDays(50))
                .dueDate(FIXED_TODAY.minusDays(35))
                .status(LoanStatus.ACTIVE)
                .fineAmount(BigDecimal.ZERO)
                .build());

        Loan overdue = loanService.markOverdue(loan.getId());
        assertThat(overdue.getStatus()).isEqualTo(LoanStatus.OVERDUE);

        loanService.requestReturn(loan.getId());
        Loan returned = loanService.confirmReturn(loan.getId());

        // 35 days overdue, high band at 2.00/day.
        assertThat(returned.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(returned.getFineAmount()).isEqualByComparingTo("70.00");
    }
}
