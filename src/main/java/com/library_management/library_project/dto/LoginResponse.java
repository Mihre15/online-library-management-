package com.library_management.library_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
    @JsonProperty("token") String token,
    @JsonProperty("studentId") Long studentId
) {}
