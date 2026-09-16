package com.library_management.library_project.integration;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import com.library_management.library_project.dto.AddBookRequest;
import com.library_management.library_project.dto.BorrowRequest;
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
 * Integration test: HTTP layer (LoanController) + LoanService + all three repositories
 * against a real (in-memory) database, covering the borrow/return state transitions
 * end to end through the REST API.
 *
 * Borrowing derives the member from the caller's JWT (not a request field), and the
 * eligibility decision is reported back as {@code 200 OK} with a {@code success}
 * flag rather than an HTTP error (matching {@code BorrowResponse} / the spec in the
 * project README) — so "no copies left" is asserted as {@code success: false}, not
 * a 4xx status. Returning is two-step: the student requests a return (-> RETURN_PENDING),
 * and only an admin's confirm-return finalizes it (-> RETURNED) with the fine applied.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LoanControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private String studentToken;
    private String adminToken;
    private Long bookId;

    @BeforeEach
    void setUp() throws Exception {
        memberRepository.save(Member.builder()
                .fullName("Ada Lovelace")
                .email("ada@example.com")
                .passwordHash(passwordEncoder.encode("Password123"))
                .status(MembershipStatus.ACTIVE)
                .role(MemberRole.STUDENT)
                .registeredAt(LocalDate.now())
                .outstandingFines(BigDecimal.ZERO)
                .build());
        memberRepository.save(Member.builder()
                .fullName("Library Administrator")
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode("Password123"))
                .status(MembershipStatus.ACTIVE)
                .role(MemberRole.ADMIN)
                .registeredAt(LocalDate.now())
                .outstandingFines(BigDecimal.ZERO)
                .build());

        studentToken = loginAndGetToken("ada@example.com");
        adminToken = loginAndGetToken("admin@example.com");

        String bookBody = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddBookRequest("Clean Code", "Robert Martin", "isbn-1", "Software", 1))))
                .andReturn()
                .getResponse()
                .getContentAsString();
        bookId = objectMapper.readTree(bookBody).get("id").asLong();
    }

    private String loginAndGetToken(String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Password123"
                                }
                                """.formatted(email)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void borrow_thenReturn_transitionsThroughTheLifecycle() throws Exception {
        String loanBody = mockMvc.perform(post("/api/loans/borrow")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BorrowRequest(bookId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long loanId = objectMapper.readTree(loanBody).get("loanId").asLong();

        mockMvc.perform(post("/api/loans/{id}/return", loanId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RETURN_PENDING")));

        mockMvc.perform(post("/api/loans/{id}/confirm-return", loanId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RETURNED")))
                .andExpect(jsonPath("$.fineCharged", is(0)));
    }

    @Test
    void borrow_whenNoCopiesLeft_isReportedAsUnsuccessfulNotAnHttpError() throws Exception {
        // Ada takes the library's only copy.
        mockMvc.perform(post("/api/loans/borrow")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BorrowRequest(bookId))));

        memberRepository.save(Member.builder()
                .fullName("Grace Hopper")
                .email("grace@example.com")
                .passwordHash(passwordEncoder.encode("Password123"))
                .status(MembershipStatus.ACTIVE)
                .role(MemberRole.STUDENT)
                .registeredAt(LocalDate.now())
                .outstandingFines(BigDecimal.ZERO)
                .build());
        String secondStudentToken = loginAndGetToken("grace@example.com");

        mockMvc.perform(post("/api/loans/borrow")
                        .header("Authorization", "Bearer " + secondStudentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BorrowRequest(bookId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.denialReason", is("No copies of this book are available")));
    }

    @Test
    void requestReturn_twice_secondCallIsRejected() throws Exception {
        String loanBody = mockMvc.perform(post("/api/loans/borrow")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BorrowRequest(bookId))))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long loanId = objectMapper.readTree(loanBody).get("loanId").asLong();

        mockMvc.perform(post("/api/loans/{id}/return", loanId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/loans/{id}/return", loanId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isConflict());
    }

    @Test
    void confirmReturn_withoutAdminToken_isForbidden() throws Exception {
        String loanBody = mockMvc.perform(post("/api/loans/borrow")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BorrowRequest(bookId))))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long loanId = objectMapper.readTree(loanBody).get("loanId").asLong();

        mockMvc.perform(post("/api/loans/{id}/return", loanId)
                .header("Authorization", "Bearer " + studentToken));

        mockMvc.perform(post("/api/loans/{id}/confirm-return", loanId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }
}
