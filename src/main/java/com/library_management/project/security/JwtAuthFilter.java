package com.library_management.project.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.library_management.project.entity.MemberRole;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String STUDENT_ID_ATTR = "studentId";
    public static final String ROLE_ATTR = "role";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        boolean protectedPath = requiresToken(request);

        if (header == null || !header.startsWith("Bearer ")) {
            if (protectedPath) {
                writeUnauthorized(response, "Missing or invalid token");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        try {
            Long studentId = jwtService.parseStudentId(token);
            MemberRole role = jwtService.parseRole(token);
            request.setAttribute(STUDENT_ID_ATTR, studentId);
            request.setAttribute(ROLE_ATTR, role);
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException ex) {
            writeUnauthorized(response, "Invalid or expired token");
        }
    }

    static boolean requiresToken(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if ("POST".equals(method) && "/api/auth/login".equals(path)) {
            return false;
        }
        if ("GET".equals(method)
                && (path.equals("/api/books") || path.matches("/api/books/\\d+"))) {
            return false;
        }
        return path.startsWith("/api/");
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter()
                .write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}");
    }
}
