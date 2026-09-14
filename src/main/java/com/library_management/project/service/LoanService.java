package com.library_management.project.service;

import com.library_management.project.entity.Book;
import com.library_management.project.entity.Loan;
import com.library_management.project.entity.LoanStatus;
import com.library_management.project.entity.Member;
import com.library_management.project.exception.InvalidLoanStateException;
import com.library_management.project.exception.LoanNotAllowedException;
import com.library_management.project.exception.NotFoundException;
import com.library_management.project.repository.BookRepository;
import com.library_management.project.repository.LoanRepository;
import com.library_management.project.repository.MemberRepository;
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
 *   ACTIVE --(returnBook, on time)--&gt;  RETURNED
 *   ACTIVE --(markOverdue, past due)--&gt; OVERDUE
 *   OVERDUE --(returnBook)--&gt;          RETURNED
 *   ACTIVE --(reportLost)--&gt;           LOST
 *   OVERDUE --(reportLost)--&gt;          LOST
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
        // Serialize inventory decisions for this book so concurrent borrowers cannot
        // both decrement the same available copy.
        Book book = bookRepository.findByIdForUpdate(bookId)
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
                    "Cannot request a return for a loan in status " + loan.getStatus());
        }
        loan.setStatus(LoanStatus.RETURN_PENDING);
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan confirmReturn(Long loanId) {
        Loan loan = getById(loanId);
        if (loan.getStatus() != LoanStatus.RETURN_PENDING) {
            throw new InvalidLoanStateException(
                    "Cannot confirm a return for a loan in status " + loan.getStatus());
        }
        return completeReturn(loan);
    }

    @Transactional
    public Loan returnBook(Long loanId) {
        Loan loan = getById(loanId);
        if (loan.getStatus() == LoanStatus.RETURN_PENDING) {
            return completeReturn(loan);
        }
        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new InvalidLoanStateException(
                    "Cannot return a loan in status " + loan.getStatus());
        }

        return completeReturn(loan);
    }

    private Loan completeReturn(Loan loan) {
        LocalDate today = LocalDate.now(clock);
        long daysOverdue = Math.max(0, ChronoUnit.DAYS.between(loan.getDueDate(), today));
        var fine = fineCalculator.calculate((int) daysOverdue);

        loan.setReturnDate(today);
        loan.setStatus(LoanStatus.RETURNED);
        loan.setFineAmount(fine);

        // Lock the inventory row before restoring a copy to serialize with borrows.
        Book book = bookRepository.findByIdForUpdate(loan.getBook().getId())
                .orElseThrow(() -> new NotFoundException("Book not found: " + loan.getBook().getId()));
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

        Book book = bookRepository.findByIdForUpdate(loan.getBook().getId())
                .orElseThrow(() -> new NotFoundException("Book not found: " + loan.getBook().getId()));
        book.setTotalCopies(Math.max(0, book.getTotalCopies() - 1));
        bookRepository.save(book);

        return loanRepository.save(loan);
    }

    public Loan getById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan not found: " + id));
    }

    public Loan getByIdWithAssociations(Long id) {
        return loanRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new NotFoundException("Loan not found: " + id));
    }

    public List<Loan> listForMember(Long memberId) {
        return loanRepository.findByMemberId(memberId);
    }

    /**
     * If the loan is ACTIVE and past its due date, persist OVERDUE. Called from spec aliases
     * whenever loans are read or before borrow/return.
     */
    @Transactional
    public Loan checkAndUpdateOverdue(Loan loan) {
        LocalDate today = LocalDate.now(clock);
        if (loan.getStatus() == LoanStatus.ACTIVE && today.isAfter(loan.getDueDate())) {
            loan.setStatus(LoanStatus.OVERDUE);
            return loanRepository.save(loan);
        }
        return loan;
    }

    @Transactional
    public void refreshOverdueForMember(Long memberId) {
        for (Loan loan : loanRepository.findByMemberId(memberId)) {
            checkAndUpdateOverdue(loan);
        }
    }

    @Transactional
    public List<Loan> listForMemberRefreshingOverdue(Long memberId) {
        refreshOverdueForMember(memberId);
        return loanRepository.findByMemberIdWithAssociations(memberId);
    }
}
