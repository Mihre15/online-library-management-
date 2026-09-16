package com.library_management.project.integration;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import com.library_management.project.dto.RegisterMemberRequest;
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

    @Test
    void register_thenFetch_roundTripsThroughTheDatabase() throws Exception {
        var request = new RegisterMemberRequest("Ada Lovelace", "ada@example.com");

        String body = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(get("/api/members/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("ada@example.com")));
    }

    @Test
    void register_duplicateEmail_returnsBadRequest() throws Exception {
        var request = new RegisterMemberRequest("Ada Lovelace", "ada@example.com");
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_blankName_returnsValidationError() throws Exception {
        var request = new RegisterMemberRequest("", "ada@example.com");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_unknownMember_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/members/{id}", 999)).andExpect(status().isNotFound());
    }

    @Test
    void suspendThenReactivate_updatesStatus() throws Exception {
        var request = new RegisterMemberRequest("Ada Lovelace", "ada@example.com");
        String body = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long id = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(put("/api/members/{id}/suspend", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUSPENDED")));

        mockMvc.perform(put("/api/members/{id}/reactivate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }
}
