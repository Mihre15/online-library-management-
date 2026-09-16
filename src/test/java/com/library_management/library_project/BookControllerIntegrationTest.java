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

    private Long addBook(String isbn, String category) throws Exception {
        var request = new AddBookRequest("Clean Code", "Robert Martin", isbn, category, 3);
        String body = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
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
    void addCopies_increasesAvailableCopies() throws Exception {
        Long id = addBook("isbn-1", "Software");

        mockMvc.perform(put("/api/books/{id}/copies", id).param("count", "2"))
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
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
    }
}
