package com.library_management.project.controller;

import com.library_management.project.dto.AddBookRequest;
import com.library_management.project.dto.BookResponse;
import com.library_management.project.service.BookService;
import com.library_management.project.security.RequestAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookResponse> addBook(
            @Valid @RequestBody AddBookRequest request, HttpServletRequest httpRequest) {
        RequestAuth.requireAdmin(httpRequest);
        var book = bookService.addBook(
                request.title(), request.author(), request.isbn(), request.category(), request.totalCopies());
        return ResponseEntity.status(HttpStatus.CREATED).body(BookResponse.from(book));
    }

    @GetMapping("/{id}")
    public BookResponse getById(@PathVariable Long id) {
        return BookResponse.from(bookService.getById(id));
    }

    @GetMapping
    public List<BookResponse> listAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String query) {
        List<BookResponse> books;
        if (query != null && !query.isBlank()) {
            books = bookService.searchByQuery(query).stream().map(BookResponse::from).toList();
        } else if (category == null) {
            books = bookService.listAll().stream().map(BookResponse::from).toList();
        } else {
            books = bookService.searchByCategory(category).stream().map(BookResponse::from).toList();
        }
        return books;
    }

    @PutMapping("/{id}/copies")
    public BookResponse addCopies(
            @PathVariable Long id, @RequestParam int count, HttpServletRequest httpRequest) {
        RequestAuth.requireAdmin(httpRequest);
        return BookResponse.from(bookService.addCopies(id, count));
    }
}
