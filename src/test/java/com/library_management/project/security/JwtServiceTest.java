package com.library_management.project.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.library_management.project.entity.MemberRole;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtService jwtService =
            new JwtService("library-management-test-secret-key-32b", 3_600_000);

    @Test
    void roundTripsStudentId() {
        String token = jwtService.createToken(42L, "ada@library.test", MemberRole.STUDENT);

        assertThat(jwtService.parseStudentId(token)).isEqualTo(42L);
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtService.createToken(42L, "ada@library.test", MemberRole.STUDENT);

        assertThatThrownBy(() -> jwtService.parseStudentId(token + "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsExpiredToken() {
        JwtService shortLived = new JwtService("library-management-test-secret-key-32b", -1_000);

        String token = shortLived.createToken(1L, "ada@library.test", MemberRole.STUDENT);

        assertThatThrownBy(() -> shortLived.parseStudentId(token)).isInstanceOf(IllegalArgumentException.class);
    }
}
