package com.library_management.library_project.service;

import com.library_management.library_project.dto.LoginResponse;
import com.library_management.library_project.entity.Member;
import com.library_management.library_project.entity.MemberRole;
import com.library_management.library_project.entity.MembershipStatus;
import com.library_management.library_project.exception.AccountSuspendedException;
import com.library_management.library_project.exception.AuthenticationException;
import com.library_management.library_project.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private Clock clock;

  private AuthService authService;
  private BCryptPasswordEncoder passwordEncoder;
  private static final String JWT_SECRET = "library-management-demo-secret-key-32-bytes";
  private static final long JWT_EXPIRATION_MS = 86400000L;

  @BeforeEach
  void setUp() {
    Clock fixedClock = Clock.fixed(
        Instant.parse("2024-01-01T00:00:00Z"),
        ZoneId.of("UTC")
    );
    authService = new AuthService(memberRepository, fixedClock, JWT_SECRET, JWT_EXPIRATION_MS);
    passwordEncoder = new BCryptPasswordEncoder();
  }

  @Test
  void register_succeeds_with_valid_input() {
    String email = "newuser@test.com";
    String fullName = "New User";
    String password = "Password123";

    when(memberRepository.existsByEmail(email)).thenReturn(false);
    when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
      Member member = invocation.getArgument(0);
      member.setId(1L);
      return member;
    });

    LoginResponse response = authService.register(fullName, email, password);

    assertThat(response).isNotNull();
    assertThat(response.token()).isNotBlank();
    assertThat(response.studentId()).isEqualTo(1L);

    verify(memberRepository).existsByEmail(email);
    verify(memberRepository).save(any(Member.class));
  }

  @Test
  void register_fails_with_duplicate_email() {
    String email = "existing@test.com";
    String fullName = "New User";
    String password = "Password123";

    when(memberRepository.existsByEmail(email)).thenReturn(true);

    assertThatThrownBy(() -> authService.register(fullName, email, password))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email already registered");

    verify(memberRepository).existsByEmail(email);
    verify(memberRepository, never()).save(any());
  }

  @Test
  void login_succeeds_with_correct_credentials() {
    String email = "user@test.com";
    String password = "Password123";
    String hashedPassword = passwordEncoder.encode(password);

    Member member = Member.builder()
        .id(1L)
        .fullName("Test User")
        .email(email)
        .passwordHash(hashedPassword)
        .status(MembershipStatus.ACTIVE)
        .role(MemberRole.STUDENT)
        .registeredAt(LocalDate.now())
        .outstandingFines(BigDecimal.ZERO)
        .build();

    when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

    LoginResponse response = authService.login(email, password);

    assertThat(response).isNotNull();
    assertThat(response.token()).isNotBlank();
    assertThat(response.studentId()).isEqualTo(1L);

    verify(memberRepository).findByEmail(email);
  }

  @Test
  void login_fails_with_incorrect_password() {
    String email = "user@test.com";
    String correctPassword = "Password123";
    String wrongPassword = "WrongPassword";
    String hashedPassword = passwordEncoder.encode(correctPassword);

    Member member = Member.builder()
        .id(1L)
        .fullName("Test User")
        .email(email)
        .passwordHash(hashedPassword)
        .status(MembershipStatus.ACTIVE)
        .role(MemberRole.STUDENT)
        .registeredAt(LocalDate.now())
        .outstandingFines(BigDecimal.ZERO)
        .build();

    when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

    assertThatThrownBy(() -> authService.login(email, wrongPassword))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid email or password");

    verify(memberRepository).findByEmail(email);
  }

  @Test
  void login_fails_with_nonexistent_email() {
    String email = "nonexistent@test.com";
    String password = "Password123";

    when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(email, password))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid email or password");

    verify(memberRepository).findByEmail(email);
  }

  @Test
  void login_fails_with_suspended_account() {
    String email = "suspended@test.com";
    String password = "Password123";
    String hashedPassword = passwordEncoder.encode(password);

    Member member = Member.builder()
        .id(1L)
        .fullName("Suspended User")
        .email(email)
        .passwordHash(hashedPassword)
        .status(MembershipStatus.SUSPENDED)
        .role(MemberRole.STUDENT)
        .registeredAt(LocalDate.now())
        .outstandingFines(BigDecimal.ZERO)
        .build();

    when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

    assertThatThrownBy(() -> authService.login(email, password))
        .isInstanceOf(AccountSuspendedException.class)
        .hasMessage("Account is suspended");

    verify(memberRepository).findByEmail(email);
  }
}
