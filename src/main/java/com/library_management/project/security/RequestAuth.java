package com.library_management.project.security;

import com.library_management.project.exception.UnauthorizedException;
import com.library_management.project.entity.MemberRole;
import com.library_management.project.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;

public final class RequestAuth {

    private RequestAuth() {}

    public static Long requireStudentId(HttpServletRequest request) {
        Object value = request.getAttribute(JwtAuthFilter.STUDENT_ID_ATTR);
        if (value instanceof Long studentId) {
            return studentId;
        }
        throw new UnauthorizedException("Missing or invalid token");
    }

    public static void requireAdmin(HttpServletRequest request) {
        requireStudentId(request);
        Object value = request.getAttribute(JwtAuthFilter.ROLE_ATTR);
        if (value != MemberRole.ADMIN) {
            throw new ForbiddenException("Administrator access required");
        }
    }
}
