package com.library_management.library_project.integration;

import com.library_management.library_project.dto.LoginRequest;
import com.library_management.library_project.dto.RegisterRequest;
import com.library_management.library_project.entity.Member;
import com.library_management.library_project.entity.MemberRole;
import com.library_management.library_project.entity.MembershipStatus;
import com.library_management.library_project.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  private static final String BASE_URL = "/api/auth";

  @BeforeEach
  void setUp() {
    memberRepository.deleteAll();
  }

  @Test
  void register_creates_new_member_and_returns_token() throws Exception {
    RegisterRequest request = new RegisterRequest(
        "Test User",
        "test@example.com",
        "Password123"
    );

    mockMvc.perform(post(BASE_URL + "/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "fullName": "Test User",
              "email": "test@example.com",
              "password": "Password123"
            }
            """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token", notNullValue()))
        .andExpect(jsonPath("$.studentId", notNullValue()));

    // Verify member was created
    var member = memberRepository.findByEmail("test@example.com");
    assert member.isPresent();
    assert member.get().getFullName().equals("Test User");
  }

  @Test
  void register_fails_with_duplicate_email() throws Exception {
    // Create existing member
    Member existing = Member.builder()
        .fullName("Existing User")
        .email("existing@example.com")
        .passwordHash(passwordEncoder.encode("Password123"))
        .status(MembershipStatus.ACTIVE)
        .role(MemberRole.STUDENT)
        .registeredAt(LocalDate.now())
        .outstandingFines(BigDecimal.ZERO)
        .build();
    memberRepository.save(existing);

    mockMvc.perform(post(BASE_URL + "/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "fullName": "Another User",
              "email": "existing@example.com",
              "password": "Password123"
            }
            """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_succeeds_with_correct_credentials() throws Exception {
    // Create a member
    Member member = Member.builder()
        .fullName("Test User")
        .email("login@example.com")
        .passwordHash(passwordEncoder.encode("Password123"))
        .status(MembershipStatus.ACTIVE)
        .role(MemberRole.STUDENT)
        .registeredAt(LocalDate.now())
        .outstandingFines(BigDecimal.ZERO)
        .build();
    memberRepository.save(member);

    mockMvc.perform(post(BASE_URL + "/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "email": "login@example.com",
              "password": "Password123"
            }
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token", notNullValue()))
        .andExpect(jsonPath("$.studentId", notNullValue()));
  }

  @Test
  void login_fails_with_incorrect_password() throws Exception {
    // Create a member
    Member member = Member.builder()
        .fullName("Test User")
        .email("login@example.com")
        .passwordHash(passwordEncoder.encode("CorrectPassword123"))
        .status(MembershipStatus.ACTIVE)
        .role(MemberRole.STUDENT)
        .registeredAt(LocalDate.now())
        .outstandingFines(BigDecimal.ZERO)
        .build();
    memberRepository.save(member);

    mockMvc.perform(post(BASE_URL + "/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "email": "login@example.com",
              "password": "WrongPassword123"
            }
            """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_fails_with_nonexistent_email() throws Exception {
    mockMvc.perform(post(BASE_URL + "/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "email": "nonexistent@example.com",
              "password": "Password123"
            }
            """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_fails_with_suspended_account() throws Exception {
    // Create a suspended member
    Member member = Member.builder()
        .fullName("Suspended User")
        .email("suspended@example.com")
        .passwordHash(passwordEncoder.encode("Password123"))
        .status(MembershipStatus.SUSPENDED)
        .role(MemberRole.STUDENT)
        .registeredAt(LocalDate.now())
        .outstandingFines(BigDecimal.ZERO)
        .build();
    memberRepository.save(member);

    mockMvc.perform(post(BASE_URL + "/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "email": "suspended@example.com",
              "password": "Password123"
            }
            """))
        .andExpect(status().isForbidden());
  }
}
