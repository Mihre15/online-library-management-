package com.library_management.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.library_management.project.entity.Member;
import com.library_management.project.entity.MemberRole;
import com.library_management.project.entity.MembershipStatus;
import com.library_management.project.exception.UnauthorizedException;
import com.library_management.project.repository.MemberRepository;
import com.library_management.project.security.JwtService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private JwtService jwtService;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(memberRepository, encoder, jwtService);
    }

    private Member memberWithPassword(String rawPassword) {
        return Member.builder()
                .id(7L)
                .fullName("Ada")
                .email("ada@library.test")
                .passwordHash(encoder.encode(rawPassword))
                .status(MembershipStatus.ACTIVE)
                .registeredAt(LocalDate.now())
                .outstandingFines(BigDecimal.ZERO)
                .build();
    }

    @Test
    void login_validCredentials_returnsTokenAndStudentId() {
        when(memberRepository.findByEmail("ada@library.test")).thenReturn(Optional.of(memberWithPassword("Password123")));
        when(jwtService.createToken(7L, "ada@library.test", MemberRole.STUDENT)).thenReturn("jwt-token");

        var result = authService.login("ada@library.test", "Password123");

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.studentId()).isEqualTo(7L);
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        when(memberRepository.findByEmail("ada@library.test")).thenReturn(Optional.of(memberWithPassword("Password123")));

        assertThatThrownBy(() -> authService.login("ada@library.test", "nope"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_unknownEmail_throwsUnauthorized() {
        when(memberRepository.findByEmail("missing@library.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("missing@library.test", "Password123"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_memberWithoutPassword_throwsUnauthorized() {
        Member member = memberWithPassword("Password123");
        member.setPasswordHash(null);
        when(memberRepository.findByEmail("ada@library.test")).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> authService.login("ada@library.test", "Password123"))
                .isInstanceOf(UnauthorizedException.class);
    }
}
