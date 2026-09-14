package com.library_management.project.repository;

import com.library_management.project.entity.Loan;
import com.library_management.project.entity.LoanStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByMemberId(Long memberId);

    List<Loan> findByMemberIdAndStatusIn(Long memberId, List<LoanStatus> statuses);

    long countByMemberIdAndStatusIn(Long memberId, List<LoanStatus> statuses);

    List<Loan> findByStatus(LoanStatus status);

    @Query("SELECT DISTINCT l FROM Loan l JOIN FETCH l.book JOIN FETCH l.member WHERE l.member.id = :memberId")
    List<Loan> findByMemberIdWithAssociations(@Param("memberId") Long memberId);

    @Query("SELECT l FROM Loan l JOIN FETCH l.book JOIN FETCH l.member WHERE l.id = :id")
    Optional<Loan> findByIdWithAssociations(@Param("id") Long id);
}
