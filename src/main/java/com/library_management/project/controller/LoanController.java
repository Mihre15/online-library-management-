package com.library_management.project.controller;

import com.library_management.project.dto.BorrowBookRequest;
import com.library_management.project.dto.BorrowRequest;
import com.library_management.project.dto.BorrowResponse;
import com.library_management.project.dto.LoanResponse;
import com.library_management.project.dto.ReturnBookResponse;
import com.library_management.project.entity.Loan;
import com.library_management.project.exception.ForbiddenException;
import com.library_management.project.exception.LoanNotAllowedException;
import com.library_management.project.security.RequestAuth;
import com.library_management.project.service.DenialReasonMapper;
import com.library_management.project.service.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    public ResponseEntity<LoanResponse> borrow(
            @Valid @RequestBody BorrowRequest request, HttpServletRequest httpRequest) {
        RequestAuth.requireAdmin(httpRequest);
        var loan = loanService.borrowBook(request.memberId(), request.bookId());
        return ResponseEntity.status(HttpStatus.CREATED).body(LoanResponse.from(loan));
    }

    @PostMapping("/borrow")
    public BorrowResponse borrowAlias(
            @Valid @RequestBody BorrowBookRequest request, HttpServletRequest httpRequest) {
        Long studentId = RequestAuth.requireStudentId(httpRequest);
        loanService.refreshOverdueForMember(studentId);
        try {
            Loan loan = loanService.borrowBook(studentId, request.bookId());
            return BorrowResponse.ok(loan.getId());
        } catch (LoanNotAllowedException ex) {
            return BorrowResponse.denied(DenialReasonMapper.toSpecReason(ex.getMessage()));
        }
    }

    @PutMapping("/{id}/return")
    public LoanResponse returnBook(@PathVariable Long id, HttpServletRequest httpRequest) {
        RequestAuth.requireAdmin(httpRequest);
        return LoanResponse.from(loanService.returnBook(id));
    }

    @PostMapping("/{id}/return")
    public ReturnBookResponse requestReturn(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long studentId = RequestAuth.requireStudentId(httpRequest);
        Loan loan = loanService.getByIdWithAssociations(id);
        if (!loan.getMember().getId().equals(studentId)) {
            throw new ForbiddenException("Cannot return another student's loan");
        }
        loanService.checkAndUpdateOverdue(loan);
        Loan pending = loanService.requestReturn(id);
        return ReturnBookResponse.pending(pending.getId());
    }

    @PostMapping("/{id}/confirm-return")
    public LoanResponse confirmReturn(@PathVariable Long id, HttpServletRequest httpRequest) {
        RequestAuth.requireAdmin(httpRequest);
        return LoanResponse.from(loanService.confirmReturn(id));
    }

    @PutMapping("/{id}/mark-overdue")
    public LoanResponse markOverdue(@PathVariable Long id, HttpServletRequest httpRequest) {
        RequestAuth.requireAdmin(httpRequest);
        return LoanResponse.from(loanService.markOverdue(id));
    }

    @PutMapping("/{id}/report-lost")
    public LoanResponse reportLost(@PathVariable Long id, HttpServletRequest httpRequest) {
        RequestAuth.requireAdmin(httpRequest);
        return LoanResponse.from(loanService.reportLost(id));
    }

    @GetMapping("/{id}")
    public LoanResponse getById(@PathVariable Long id, HttpServletRequest httpRequest) {
        RequestAuth.requireAdmin(httpRequest);
        return LoanResponse.from(loanService.getById(id));
    }

    @GetMapping("/member/{memberId}")
    public List<LoanResponse> listForMember(
            @PathVariable Long memberId, HttpServletRequest httpRequest) {
        RequestAuth.requireAdmin(httpRequest);
        return loanService.listForMember(memberId).stream().map(LoanResponse::from).toList();
    }
}
