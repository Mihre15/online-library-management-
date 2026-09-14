package com.library_management.project.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import com.library_management.project.dto.AddBookRequest;
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
 * Integration test: HTTP layer (BookController) + BookService + BookRepository
 * against a real (in-memory) database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookControllerIntegrationTest {

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

    private Long addBook(String isbn, String category) throws Exception {
        var request = new AddBookRequest("Clean Code", "Robert Martin", isbn, category, 3);
        String body = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    void addBook_availableCopiesStartsEqualToTotal() throws Exception {
        var request = new AddBookRequest("Clean Code", "Robert Martin", "isbn-1", "Software", 3);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalCopies", is(3)))
                .andExpect(jsonPath("$.availableCopies", is(3)));
    }

    @Test
    void searchByCategory_filtersResults() throws Exception {
        addBook("isbn-1", "Software");
        addBook("isbn-2", "Fiction");

        mockMvc.perform(get("/api/books").param("category", "Software"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].isbn", is("isbn-1")));
    }

    @Test
    void searchByQuery_matchesTitleAuthorOrIsbn() throws Exception {
        addBook("isbn-1", "Software");
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(
                                new AddBookRequest("Domain-Driven Design", "Eric Evans", "isbn-2", "Software", 1))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/books").param("query", "evans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].isbn", is("isbn-2")))
                .andExpect(jsonPath("$[0].copiesAvailable", is(1)))
                .andExpect(jsonPath("$[0].availableCopies", is(1)));
    }

    @Test
    void addCopies_increasesAvailableCopies() throws Exception {
        Long id = addBook("isbn-1", "Software");

        mockMvc.perform(put("/api/books/{id}/copies", id).param("count", "2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCopies", is(5)))
                .andExpect(jsonPath("$.availableCopies", is(5)));
    }

    @Test
    void addBook_duplicateIsbn_returnsBadRequest() throws Exception {
        addBook("isbn-1", "Software");
        var duplicate = new AddBookRequest("Another Title", "Someone", "isbn-1", "Software", 1);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
    }
}
