package com.library_management.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterMemberRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email) {
}
