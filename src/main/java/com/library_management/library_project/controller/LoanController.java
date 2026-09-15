package com.library_management.library_project.controller;

import com.library_management.library_project.dto.AdminLoanResponse;
import com.library_management.library_project.dto.BorrowRequest;
import com.library_management.library_project.dto.BorrowResponse;
import com.library_management.library_project.dto.LoanResponse;
import com.library_management.library_project.dto.ReturnResponse;
import com.library_management.library_project.dto.StudentLoanResponse;
import com.library_management.library_project.entity.Loan;
import com.library_management.library_project.exception.LoanNotAllowedException;
import com.library_management.library_project.service.LoanService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class LoanController {

  private final LoanService loanService;
  private final String jwtSecret;

  public LoanController(LoanService loanService, @Value("${app.jwt.secret}") String jwtSecret) {
    this.loanService = loanService;
    this.jwtSecret = jwtSecret;
  }

  @PostMapping("/loans/borrow")
  public BorrowResponse borrowBook(
      @Valid @RequestBody BorrowRequest request,
      HttpServletRequest httpRequest) {
    Long memberId = extractMemberIdFromToken(httpRequest);
    try {
      Loan loan = loanService.borrowBook(memberId, request.bookId());
      return BorrowResponse.allowed(loan.getId());
    } catch (LoanNotAllowedException e) {
      return BorrowResponse.denied(e.getMessage());
    }
  }

  @PostMapping("/loans/{id}/return")
  public ReturnResponse returnBook(@PathVariable Long id) {
    Loan loan = loanService.requestReturn(id);
    return new ReturnResponse(
        true,
        loan.getStatus().name(),
        loan.getFineAmount(),
        "Return requested. Bring the book to the library desk for confirmation."
    );
  }

  @PostMapping("/loans/{id}/confirm-return")
  @PreAuthorize("hasRole('ADMIN')")
  public ReturnResponse confirmReturn(@PathVariable Long id) {
    Loan loan = loanService.confirmReturn(id);
    return new ReturnResponse(
        true,
        loan.getStatus().name(),
        loan.getFineAmount(),
        "Return confirmed."
    );
  }

  @GetMapping("/loans/{id}")
  public LoanResponse getLoan(@PathVariable Long id) {
    Loan loan = loanService.getById(id);
    return LoanResponse.from(loan);
  }

  @GetMapping("/loans")
  @PreAuthorize("hasRole('ADMIN')")
  public List<AdminLoanResponse> listAllLoans() {
    return loanService.listAll()
        .stream()
        .map(AdminLoanResponse::from)
        .toList();
  }

  @PutMapping("/loans/{id}/mark-overdue")
  @PreAuthorize("hasRole('ADMIN')")
  public AdminLoanResponse markOverdue(@PathVariable Long id) {
    Loan loan = loanService.markOverdue(id);
    return AdminLoanResponse.from(loan);
  }

  @PutMapping("/loans/{id}/report-lost")
  @PreAuthorize("hasRole('ADMIN')")
  public AdminLoanResponse reportLost(@PathVariable Long id) {
    Loan loan = loanService.reportLost(id);
    return AdminLoanResponse.from(loan);
  }

  @GetMapping("/students/{id}/loans")
  public List<StudentLoanResponse> getStudentLoans(@PathVariable Long id) {
    return loanService.listForMember(id)
        .stream()
        .map(StudentLoanResponse::from)
        .toList();
  }

  private Long extractMemberIdFromToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new IllegalArgumentException("Missing or invalid authorization header");
    }

    String token = authHeader.substring(7);
    try {
      SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
      String subject = Jwts.parser()
          .verifyWith(key)
          .build()
          .parseSignedClaims(token)
          .getPayload()
          .getSubject();
      return Long.parseLong(subject);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid token");
    }
  }
}
