package com.library_management.project.controller;

import com.library_management.project.dto.StudentLoanResponse;
import com.library_management.project.exception.ForbiddenException;
import com.library_management.project.security.RequestAuth;
import com.library_management.project.service.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final LoanService loanService;

    public StudentController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping("/{id}/loans")
    public List<StudentLoanResponse> listLoans(@PathVariable Long id, HttpServletRequest request) {
        Long studentId = RequestAuth.requireStudentId(request);
        if (!studentId.equals(id)) {
            throw new ForbiddenException("Cannot view another student's loans");
        }
        return loanService.listForMemberRefreshingOverdue(id).stream()
                .map(StudentLoanResponse::from)
                .toList();
    }
}
