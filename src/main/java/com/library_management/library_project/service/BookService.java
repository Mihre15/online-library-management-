package com.library_management.library_project.service;

import com.library_management.library_project.entity.Book;
import com.library_management.library_project.exception.NotFoundException;
import com.library_management.library_project.repository.BookRepository;
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

    @Transactional
    public Book updateBook(Long id, String title, String author, String isbn, String category) {
        Book book = getById(id);
        if (!book.getIsbn().equals(isbn) && bookRepository.existsByIsbn(isbn)) {
            throw new IllegalArgumentException("A book with ISBN " + isbn + " already exists");
        }
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setCategory(category);
        return bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = getById(id);
        bookRepository.delete(book);
    }

    public List<Book> searchByTitleOrAuthorOrIsbn(String query) {
        String lowerQuery = query.toLowerCase();
        return bookRepository.findAll()
                .stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerQuery) ||
                           b.getAuthor().toLowerCase().contains(lowerQuery) ||
                           b.getIsbn().contains(query))
                .toList();
    }
}
