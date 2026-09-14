package com.library_management.project.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.library_management.project.dto.AddBookRequest;
import com.library_management.project.dto.BorrowBookRequest;
import com.library_management.project.dto.LoginRequest;
import com.library_management.project.entity.Book;
import com.library_management.project.entity.Loan;
import com.library_management.project.entity.LoanStatus;
import com.library_management.project.entity.Member;
import com.library_management.project.entity.MemberRole;
import com.library_management.project.entity.MembershipStatus;
import com.library_management.project.repository.BookRepository;
import com.library_management.project.repository.LoanRepository;
import com.library_management.project.repository.MemberRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FixedClockTestConfig.class)
@Transactional
class SpecAliasIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private LoanRepository loanRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Member saveStudent(String email, MembershipStatus status, BigDecimal fines) {
        return memberRepository.save(Member.builder()
                .fullName("Test Student")
                .email(email)
                .passwordHash(passwordEncoder.encode("Password123"))
                .status(status)
                .registeredAt(FixedClockTestConfig.FIXED_TODAY)
                .outstandingFines(fines)
                .build());
    }

    private Member saveAdmin(String email) {
        return memberRepository.save(Member.builder()
                .fullName("Library Administrator")
                .email(email)
                .passwordHash(passwordEncoder.encode("Password123"))
                .status(MembershipStatus.ACTIVE)
                .role(MemberRole.ADMIN)
                .registeredAt(FixedClockTestConfig.FIXED_TODAY)
                .outstandingFines(BigDecimal.ZERO)
                .build());
    }

    private Book saveBook(String isbn, int copies) {
        return bookRepository.save(Book.builder()
                .title("Clean Code")
                .author("Robert Martin")
                .isbn(isbn)
                .category("Software")
                .totalCopies(copies)
                .availableCopies(copies)
                .build());
    }

    private String loginToken(String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "Password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.studentId", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).get("token").asString();
    }

    @Test
    void login_wrongPassword_returnsUnauthorized() throws Exception {
        saveStudent("ada@library.test", MembershipStatus.ACTIVE, BigDecimal.ZERO);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("ada@library.test", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid email or password")));
    }

    @Test
    void borrowAlias_requiresToken() throws Exception {
        mockMvc.perform(post("/api/loans/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BorrowBookRequest(1L))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void legacyBookManagement_requiresAdminRole() throws Exception {
        Member student = saveStudent("student@library.test", MembershipStatus.ACTIVE, BigDecimal.ZERO);
        String studentToken = loginToken(student.getEmail());

        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddBookRequest("Clean Code", "Robert Martin", "isbn-student", "Software", 1))))
                .andExpect(status().isForbidden());
    }

    @Test
    void legacyBookManagement_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddBookRequest("Clean Code", "Robert Martin", "isbn-anon", "Software", 1))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void borrowAlias_success_returnsLoanId() throws Exception {
        Member member = saveStudent("ada@library.test", MembershipStatus.ACTIVE, BigDecimal.ZERO);
        Book book = saveBook("isbn-ok", 2);
        String token = loginToken("ada@library.test");

        mockMvc.perform(post("/api/loans/borrow")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BorrowBookRequest(book.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.loanId", notNullValue()));

        mockMvc.perform(get("/api/students/{id}/loans", member.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].book.title", is("Clean Code")))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }

    @Test
    void borrowAlias_suspended_returnsSpecDenialReason() throws Exception {
        saveStudent("suspended@library.test", MembershipStatus.SUSPENDED, BigDecimal.ZERO);
        Book book = saveBook("isbn-sus", 1);
        String token = loginToken("suspended@library.test");

        mockMvc.perform(post("/api/loans/borrow")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BorrowBookRequest(book.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.denialReason", is("Account suspended")));
    }

    @Test
    void borrowAlias_noCopies_returnsSpecDenialReason() throws Exception {
        saveStudent("ada@library.test", MembershipStatus.ACTIVE, BigDecimal.ZERO);
        Book book = saveBook("isbn-none", 0);
        String token = loginToken("ada@library.test");

        mockMvc.perform(post("/api/loans/borrow")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BorrowBookRequest(book.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.denialReason", is("No copies available")));
    }

    @Test
    void studentLoans_flipsPastDueActiveToOverdue() throws Exception {
        Member member = saveStudent("ada@library.test", MembershipStatus.ACTIVE, BigDecimal.ZERO);
        Book book = saveBook("isbn-late", 1);
        book.setAvailableCopies(0);
        bookRepository.save(book);
        loanRepository.save(Loan.builder()
                .member(member)
                .book(book)
                .borrowDate(FixedClockTestConfig.FIXED_TODAY.minusDays(20))
                .dueDate(FixedClockTestConfig.FIXED_TODAY.minusDays(6))
                .status(LoanStatus.ACTIVE)
                .fineAmount(BigDecimal.ZERO)
                .build());
        String token = loginToken("ada@library.test");

        mockMvc.perform(get("/api/students/{id}/loans", member.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("OVERDUE")));
    }

    @Test
    void studentLoans_otherStudent_forbidden() throws Exception {
        Member ada = saveStudent("ada@library.test", MembershipStatus.ACTIVE, BigDecimal.ZERO);
        Member other = saveStudent("other@library.test", MembershipStatus.ACTIVE, BigDecimal.ZERO);
        String token = loginToken("ada@library.test");

        mockMvc.perform(get("/api/students/{id}/loans", other.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/students/{id}/loans", ada.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void returnAlias_requestsReturnUntilAdminConfirms() throws Exception {
        Member member = saveStudent("ada@library.test", MembershipStatus.ACTIVE, BigDecimal.ZERO);
        Book book = saveBook("isbn-ret", 1);
        book.setAvailableCopies(0);
        bookRepository.save(book);
        Loan loan = loanRepository.save(Loan.builder()
                .member(member)
                .book(book)
                .borrowDate(FixedClockTestConfig.FIXED_TODAY.minusDays(3))
                .dueDate(FixedClockTestConfig.FIXED_TODAY.plusDays(11))
                .status(LoanStatus.ACTIVE)
                .fineAmount(BigDecimal.ZERO)
                .build());
        String token = loginToken("ada@library.test");

        mockMvc.perform(post("/api/loans/{id}/return", loan.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is("RETURN_PENDING")))
                .andExpect(jsonPath("$.fineCharged", is(0)));
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableCopies()).isZero();
        assertThat(loanRepository.findById(loan.getId()).orElseThrow().getStatus())
                .isEqualTo(LoanStatus.RETURN_PENDING);

        String adminToken = loginToken(saveAdmin("confirm-admin@library.test").getEmail());
        mockMvc.perform(post("/api/loans/{id}/confirm-return", loan.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RETURNED")))
                .andExpect(jsonPath("$.fineAmount", is(0)));
    }

    @Test
    void confirmReturn_requiresAdminRole() throws Exception {
        Member member = saveStudent("pending-student@library.test", MembershipStatus.ACTIVE, BigDecimal.ZERO);
        Book book = saveBook("isbn-pending", 1);
        Loan loan = loanRepository.save(Loan.builder()
                .member(member)
                .book(book)
                .borrowDate(FixedClockTestConfig.FIXED_TODAY.minusDays(3))
                .dueDate(FixedClockTestConfig.FIXED_TODAY.plusDays(11))
                .status(LoanStatus.RETURN_PENDING)
                .fineAmount(BigDecimal.ZERO)
                .build());

        String studentToken = loginToken(member.getEmail());
        mockMvc.perform(post("/api/loans/{id}/confirm-return", loan.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void addBook_includesCopiesAvailableAliasField() throws Exception {
        String adminToken = loginToken(saveAdmin("admin@library.test").getEmail());
        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddBookRequest("Clean Code", "Robert Martin", "isbn-alias", "Software", 3))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.availableCopies", is(3)))
                .andExpect(jsonPath("$.copiesAvailable", is(3)));
    }
}
