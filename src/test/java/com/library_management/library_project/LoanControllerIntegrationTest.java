package com.library_management.project.integration;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import com.library_management.project.dto.AddBookRequest;
import com.library_management.project.dto.BorrowRequest;
import com.library_management.project.dto.RegisterMemberRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test: HTTP layer (LoanController) + LoanService + all three repositories
 * against a real (in-memory) database, covering the borrow/return state transitions
 * end to end through the REST API.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FixedClockTestConfig.class)
@Transactional
class LoanControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private Long memberId;
    private Long bookId;

    @BeforeEach
    void setUp() throws Exception {
        String memberBody = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterMemberRequest("Ada Lovelace", "ada@example.com"))))
                .andReturn()
                .getResponse()
                .getContentAsString();
        memberId = objectMapper.readTree(memberBody).get("id").asLong();

        String bookBody = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddBookRequest("Clean Code", "Robert Martin", "isbn-1", "Software", 1))))
                .andReturn()
                .getResponse()
                .getContentAsString();
        bookId = objectMapper.readTree(bookBody).get("id").asLong();
    }

    @Test
    void borrow_thenReturn_transitionsThroughTheLifecycle() throws Exception {
        String loanBody = mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BorrowRequest(memberId, bookId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long loanId = objectMapper.readTree(loanBody).get("id").asLong();

        mockMvc.perform(put("/api/loans/{id}/return", loanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RETURNED")))
                .andExpect(jsonPath("$.fineAmount", is(0)));
    }

    @Test
    void borrow_whenNoCopiesLeft_returnsConflict() throws Exception {
        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BorrowRequest(memberId, bookId))));

        String secondMemberBody = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterMemberRequest("Grace Hopper", "grace@example.com"))))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long secondMemberId = objectMapper.readTree(secondMemberBody).get("id").asLong();

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BorrowRequest(secondMemberId, bookId))))
                .andExpect(status().isConflict());
    }

    @Test
    void returnBook_twice_secondCallIsRejected() throws Exception {
        String loanBody = mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BorrowRequest(memberId, bookId))))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long loanId = objectMapper.readTree(loanBody).get("id").asLong();

        mockMvc.perform(put("/api/loans/{id}/return", loanId)).andExpect(status().isOk());

        mockMvc.perform(put("/api/loans/{id}/return", loanId)).andExpect(status().isConflict());
    }
}
