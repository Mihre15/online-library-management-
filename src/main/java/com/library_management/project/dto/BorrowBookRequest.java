package com.library_management.project.dto;

import jakarta.validation.constraints.NotNull;

public record BorrowBookRequest(@NotNull Long bookId) {
}
