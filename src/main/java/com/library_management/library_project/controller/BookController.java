package com.library_management.library_project.controller;

import com.library_management.library_project.dto.AddBookRequest;
import com.library_management.library_project.dto.BookResponse;
import com.library_management.library_project.entity.Book;
import com.library_management.library_project.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:3000")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @GetMapping
  public List<BookResponse> listBooks(
      @RequestParam(required = false) String query,
      @RequestParam(required = false) String category) {
    if (query != null && !query.isBlank()) {
      return bookService.searchByTitleOrAuthorOrIsbn(query)
          .stream()
          .map(this::toResponse)
          .toList();
    }
    if (category != null && !category.isBlank()) {
      return bookService.searchByCategory(category)
          .stream()
          .map(this::toResponse)
          .toList();
    }
    return bookService.listAll()
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @GetMapping("/{id}")
  public BookResponse getBook(@PathVariable Long id) {
    Book book = bookService.getById(id);
    return toResponse(book);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public BookResponse addBook(@Valid @RequestBody AddBookRequest request) {
    Book book = bookService.addBook(
        request.title(),
        request.author(),
        request.isbn(),
        request.category(),
        request.totalCopies());
    return toResponse(book);
  }

  @PutMapping("/{id}/copies")
  @PreAuthorize("hasRole('ADMIN')")
  public BookResponse addCopies(
      @PathVariable Long id,
      @RequestParam int count) {
    Book book = bookService.addCopies(id, count);
    return toResponse(book);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public BookResponse updateBook(
      @PathVariable Long id,
      @Valid @RequestBody AddBookRequest request) {
    Book book = bookService.updateBook(
        id,
        request.title(),
        request.author(),
        request.isbn(),
        request.category());
    return toResponse(book);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteBook(@PathVariable Long id) {
    bookService.deleteBook(id);
  }

  private BookResponse toResponse(Book book) {
    return new BookResponse(
        book.getId(),
        book.getTitle(),
        book.getAuthor(),
        book.getIsbn(),
        book.getCategory(),
        book.getTotalCopies(),
        book.getAvailableCopies());
  }
}
