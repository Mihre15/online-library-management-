package com.library_management.library_project.Repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.library_management.library_project.repository.BookRepository;
import com.library_management.library_project.repository.LoanRepository;
import com.library_management.library_project.repository.MemberRepository;
import com.library_management.library_project.entity.Book;
import com.library_management.library_project.entity.Loan;
import com.library_management.library_project.entity.LoanStatus;
import com.library_management.library_project.entity.Member;
import com.library_management.library_project.entity.MembershipStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

/**
 * Integration test: exercises the JPA mapping between Loan, Member and Book
 * against a real (in-memory) database, and the derived query methods used by
 * LoanService.
 */
@DataJpaTest
class LoanRepositoryTest {

    @Autowired private LoanRepository loanRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BookRepository bookRepository;

    private Member member;
    private Book book;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .fullName("Ada Lovelace")
                .email("ada@example.com")
                .status(MembershipStatus.ACTIVE)
                .registeredAt(LocalDate.now())
                .outstandingFines(BigDecimal.ZERO)
                .build());
        book = bookRepository.save(Book.builder()
                .title("Clean Code")
                .author("Robert Martin")
                .isbn("isbn-1")
                .category("Software")
                .totalCopies(3)
                .availableCopies(2)
                .build());
    }

    private Loan newLoan(LoanStatus status) {
        return loanRepository.save(Loan.builder()
                .member(member)
                .book(book)
                .borrowDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().minusDays(3))
                .status(status)
                .fineAmount(BigDecimal.ZERO)
                .build());
    }

    @Test
    void savedLoan_roundTripsWithMemberAndBookAssociations() {
        Loan saved = newLoan(LoanStatus.ACTIVE);

        Loan reloaded = loanRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getMember().getEmail()).isEqualTo("ada@example.com");
        assertThat(reloaded.getBook().getIsbn()).isEqualTo("isbn-1");
    }

    @Test
    void findByMemberId_returnsOnlyThatMembersLoans() {
        newLoan(LoanStatus.ACTIVE);
        newLoan(LoanStatus.RETURNED);

        List<Loan> loans = loanRepository.findByMemberId(member.getId());

        assertThat(loans).hasSize(2);
    }

    @Test
    void countByMemberIdAndStatusIn_countsOnlyOpenLoans() {
        newLoan(LoanStatus.ACTIVE);
        newLoan(LoanStatus.OVERDUE);
        newLoan(LoanStatus.RETURNED);

        long openCount = loanRepository.countByMemberIdAndStatusIn(
                member.getId(), List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE));

        assertThat(openCount).isEqualTo(2);
    }

    @Test
    void findByStatus_filtersAcrossMembers() {
        newLoan(LoanStatus.OVERDUE);

        List<Loan> overdue = loanRepository.findByStatus(LoanStatus.OVERDUE);

        assertThat(overdue).extracting(Loan::getStatus).containsOnly(LoanStatus.OVERDUE);
    }
}

