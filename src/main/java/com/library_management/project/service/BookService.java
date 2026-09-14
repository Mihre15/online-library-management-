package com.library_management.project.service;

import com.library_management.project.entity.Book;
import com.library_management.project.exception.NotFoundException;
import com.library_management.project.repository.BookRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public Book addBook(String title, String author, String isbn, String category, int totalCopies) {
        if (totalCopies < 0) {
            throw new IllegalArgumentException("totalCopies cannot be negative");
        }
        if (bookRepository.existsByIsbn(isbn)) {
            throw new IllegalArgumentException("A book with ISBN " + isbn + " already exists");
        }
        Book book = Book.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .category(category)
                .totalCopies(totalCopies)
                .availableCopies(totalCopies)
                .build();
        return bookRepository.save(book);
    }

    public Book getById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found: " + id));
    }

    public List<Book> listAll() {
        return bookRepository.findAll();
    }

    public List<Book> searchByCategory(String category) {
        return bookRepository.findByCategoryIgnoreCase(category);
    }

    public List<Book> searchByQuery(String query) {
        if (query == null || query.isBlank()) {
            return listAll();
        }
        return bookRepository.searchByTitleAuthorOrIsbn(query.trim());
    }

    @Transactional
    public Book addCopies(Long id, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
        Book book = getById(id);
        book.setTotalCopies(book.getTotalCopies() + count);
        book.setAvailableCopies(book.getAvailableCopies() + count);
        return bookRepository.save(book);
    }
}
