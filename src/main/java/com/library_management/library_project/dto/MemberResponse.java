package com.library_management.library_project.dto;

import com.library_management.library_project.entity.Member;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MemberResponse(
        Long id,
        String fullName,
        String email,
        String status,
        String role,
        LocalDate registeredAt,
        BigDecimal outstandingFines) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getFullName(),
                member.getEmail(),
                member.getStatus().name(),
                member.getRole().name(),
                member.getRegisteredAt(),
                member.getOutstandingFines());
    }
}
