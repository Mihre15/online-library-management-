package com.library_management.project.integration;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import com.library_management.project.dto.RegisterMemberRequest;
import com.library_management.project.entity.Member;
import com.library_management.project.entity.MemberRole;
import com.library_management.project.entity.MembershipStatus;
import com.library_management.project.repository.MemberRepository;
import com.library_management.project.security.JwtService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test: HTTP layer (MemberController) + MemberService + MemberRepository
 * against a real (in-memory) database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private JwtService jwtService;
    private String adminToken;

    @BeforeEach
    void setUp() {
        Member admin = memberRepository.save(Member.builder()
                .fullName("Library Admin")
                .email("admin-" + System.nanoTime() + "@library.test")
                .role(MemberRole.ADMIN)
                .status(MembershipStatus.ACTIVE)
                .registeredAt(LocalDate.now())
                .outstandingFines(BigDecimal.ZERO)
                .build());
        adminToken = jwtService.createToken(admin.getId(), admin.getEmail(), MemberRole.ADMIN);
    }

    @Test
    void register_thenFetch_roundTripsThroughTheDatabase() throws Exception {
        var request = new RegisterMemberRequest("Ada Lovelace", "ada@example.com");

        String body = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(get("/api/members/{id}", id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("ada@example.com")));
    }

    @Test
    void register_duplicateEmail_returnsBadRequest() throws Exception {
        var request = new RegisterMemberRequest("Ada Lovelace", "ada@example.com");
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_blankName_returnsValidationError() throws Exception {
        var request = new RegisterMemberRequest("", "ada@example.com");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_unknownMember_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/members/{id}", 999).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void suspendThenReactivate_updatesStatus() throws Exception {
        var request = new RegisterMemberRequest("Ada Lovelace", "ada@example.com");
        String body = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long id = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(put("/api/members/{id}/suspend", id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUSPENDED")));

        mockMvc.perform(put("/api/members/{id}/reactivate", id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }
}
