package com.library_management.project.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddBookRequest(
        @NotBlank String title,
        @NotBlank String author,
        @NotBlank String isbn,
        @NotBlank String category,
        @Min(0) int totalCopies) {
}
