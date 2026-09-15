package com.library_management.library_project.repository;

import com.library_management.library_project.entity.Loan;
import com.library_management.library_project.entity.LoanStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByMemberId(Long memberId);

    List<Loan> findByMemberIdAndStatusIn(Long memberId, List<LoanStatus> statuses);

    long countByMemberIdAndStatusIn(Long memberId, List<LoanStatus> statuses);

    List<Loan> findByStatus(LoanStatus status);
}

