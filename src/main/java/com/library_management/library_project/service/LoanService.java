package com.library_management.library_project.service;

import com.library_management.library_project.entity.Book;
import com.library_management.library_project.entity.Loan;
import com.library_management.library_project.entity.LoanStatus;
import com.library_management.library_project.entity.Member;
import com.library_management.library_project.exception.InvalidLoanStateException;
import com.library_management.library_project.exception.LoanNotAllowedException;
import com.library_management.library_project.exception.NotFoundException;
import com.library_management.library_project.repository.BookRepository;
import com.library_management.library_project.repository.LoanRepository;
import com.library_management.library_project.repository.MemberRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loan lifecycle (state transition model):
 *
 * <pre>
 *   ACTIVE --(returnBook, on time)--> RETURNED
 *   ACTIVE --(markOverdue, past due)--> OVERDUE
 *   OVERDUE --(returnBook)-->          RETURNED
 *   ACTIVE --(reportLost)-->           LOST
 *   OVERDUE --(reportLost)-->          LOST
 *   RETURNED, LOST are terminal states.
 * </pre>
 */
@Service
public class LoanService {

    static final int DEFAULT_LOAN_PERIOD_DAYS = 14;
    private static final List<LoanStatus> OPEN_STATUSES =
            List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE, LoanStatus.RETURN_PENDING);

    private final LoanRepository loanRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final FineCalculator fineCalculator;
    private final LoanEligibilityService eligibilityService;
    private final Clock clock;

    public LoanService(
            LoanRepository loanRepository,
            MemberRepository memberRepository,
            BookRepository bookRepository,
            FineCalculator fineCalculator,
            LoanEligibilityService eligibilityService,
            Clock clock) {
        this.loanRepository = loanRepository;
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
        this.fineCalculator = fineCalculator;
        this.eligibilityService = eligibilityService;
        this.clock = clock;
    }

    @Transactional
    public Loan borrowBook(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found: " + memberId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found: " + bookId));

        long activeLoans = loanRepository.countByMemberIdAndStatusIn(memberId, OPEN_STATUSES);
        LoanEligibilityResult eligibility = eligibilityService.evaluate(member, book, activeLoans);
        if (!eligibility.allowed()) {
            throw new LoanNotAllowedException(String.join("; ", eligibility.reasons()));
        }

        LocalDate today = LocalDate.now(clock);
        Loan loan = Loan.builder()
                .member(member)
                .book(book)
                .borrowDate(today)
                .dueDate(today.plusDays(DEFAULT_LOAN_PERIOD_DAYS))
                .status(LoanStatus.ACTIVE)
                .build();

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan requestReturn(Long loanId) {
        Loan loan = getById(loanId);
        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new InvalidLoanStateException(
                    "Cannot return a loan in status " + loan.getStatus());
        }
        loan.setStatus(LoanStatus.RETURN_PENDING);
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan confirmReturn(Long loanId) {
        Loan loan = getById(loanId);
        if (loan.getStatus() != LoanStatus.RETURN_PENDING) {
            throw new InvalidLoanStateException(
                    "Cannot confirm a loan in status " + loan.getStatus());
        }

        LocalDate today = LocalDate.now(clock);
        long daysOverdue = Math.max(0, ChronoUnit.DAYS.between(loan.getDueDate(), today));
        var fine = fineCalculator.calculate((int) daysOverdue);

        loan.setReturnDate(today);
        loan.setStatus(LoanStatus.RETURNED);
        loan.setFineAmount(fine);

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        Member member = loan.getMember();
        member.setOutstandingFines(member.getOutstandingFines().add(fine));
        memberRepository.save(member);

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan markOverdue(Long loanId) {
        Loan loan = getById(loanId);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new InvalidLoanStateException(
                    "Cannot mark a loan in status " + loan.getStatus() + " as overdue");
        }
        LocalDate today = LocalDate.now(clock);
        if (!today.isAfter(loan.getDueDate())) {
            throw new InvalidLoanStateException("Loan " + loanId + " is not yet past its due date");
        }
        loan.setStatus(LoanStatus.OVERDUE);
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan reportLost(Long loanId) {
        Loan loan = getById(loanId);
        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new InvalidLoanStateException(
                    "Cannot report a loan in status " + loan.getStatus() + " as lost");
        }
        loan.setStatus(LoanStatus.LOST);

        Book book = loan.getBook();
        book.setTotalCopies(Math.max(0, book.getTotalCopies() - 1));
        bookRepository.save(book);

        return loanRepository.save(loan);
    }

    public Loan getById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan not found: " + id));
    }

    public List<Loan> listForMember(Long memberId) {
        return loanRepository.findByMemberId(memberId);
    }

    public List<Loan> listAll() {
        return loanRepository.findAll();
    }
}
