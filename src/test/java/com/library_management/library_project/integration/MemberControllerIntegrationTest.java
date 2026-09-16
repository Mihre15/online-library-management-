package com.library_management.library_project.integration;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import com.library_management.library_project.entity.Member;
import com.library_management.library_project.entity.MemberRole;
import com.library_management.library_project.entity.MembershipStatus;
import com.library_management.library_project.repository.MemberRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test: HTTP layer (MemberController) + MemberService + MemberRepository
 * against a real (in-memory) database. Member creation happens via
 * {@code POST /api/auth/register} (it requires a password), not through this
 * controller — MemberController itself is admin-only member management
 * (list/get/suspend/reactivate), so every test here logs in as a seeded admin
 * first, same pattern as {@link AuthControllerIntegrationTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        memberRepository.save(Member.builder()
                .fullName("Library Administrator")
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode("Password123"))
                .status(MembershipStatus.ACTIVE)
                .role(MemberRole.ADMIN)
                .registeredAt(LocalDate.now())
                .outstandingFines(BigDecimal.ZERO)
                .build());

        String loginBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@example.com",
                                  "password": "Password123"
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();
        adminToken = objectMapper.readTree(loginBody).get("token").asText();
    }

    private Long registerStudent(String fullName, String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "%s",
                                  "email": "%s",
                                  "password": "Password123"
                                }
                                """.formatted(fullName, email)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).get("studentId").asLong();
    }

    @Test
    void list_returnsEveryRegisteredMember() throws Exception {
        registerStudent("Ada Lovelace", "ada@example.com");

        mockMvc.perform(get("/api/members").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                // the seeded admin plus the newly registered student
                .andExpect(jsonPath("$.length()", is(2)));
    }

    @Test
    void list_withoutAdminToken_isForbidden() throws Exception {
        mockMvc.perform(get("/api/members")).andExpect(status().isForbidden());
    }

    @Test
    void getById_returnsThatMember() throws Exception {
        Long id = registerStudent("Ada Lovelace", "ada@example.com");

        mockMvc.perform(get("/api/members/{id}", id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("ada@example.com")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void getById_unknownMember_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/members/{id}", 999).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void suspendThenReactivate_updatesStatus() throws Exception {
        Long id = registerStudent("Ada Lovelace", "ada@example.com");

        mockMvc.perform(put("/api/members/{id}/suspend", id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUSPENDED")));

        mockMvc.perform(put("/api/members/{id}/reactivate", id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void suspend_withoutAdminToken_isForbidden() throws Exception {
        Long id = registerStudent("Ada Lovelace", "ada@example.com");

        mockMvc.perform(put("/api/members/{id}/suspend", id)).andExpect(status().isForbidden());
    }
}
