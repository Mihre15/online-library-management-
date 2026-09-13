package com.library_management.library_project.dto;

import jakarta.validation.constraints.NotNull;

public record BorrowRequest(
        @NotNull Long memberId,
        @NotNull Long bookId) {
}
