package com.library_management.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.library_management.project.entity.Book;
import com.library_management.project.exception.NotFoundException;
import com.library_management.project.repository.BookRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock private BookRepository bookRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository);
        lenient().when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void addBook_newIsbn_availableCopiesMatchesTotal() {
        when(bookRepository.existsByIsbn("isbn-1")).thenReturn(false);

        Book result = bookService.addBook("Clean Code", "Robert Martin", "isbn-1", "Software", 3);

        assertThat(result.getTotalCopies()).isEqualTo(3);
        assertThat(result.getAvailableCopies()).isEqualTo(3);
    }

    @Test
    void addBook_duplicateIsbn_throws() {
        when(bookRepository.existsByIsbn("isbn-1")).thenReturn(true);

        assertThatThrownBy(() -> bookService.addBook("Clean Code", "Robert Martin", "isbn-1", "Software", 3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addBook_negativeCopies_throws() {
        assertThatThrownBy(() -> bookService.addBook("Clean Code", "Robert Martin", "isbn-1", "Software", -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addCopies_increasesTotalAndAvailable() {
        Book book = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert Martin")
                .isbn("isbn-1")
                .category("Software")
                .totalCopies(2)
                .availableCopies(1)
                .build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.addCopies(1L, 2);

        assertThat(result.getTotalCopies()).isEqualTo(4);
        assertThat(result.getAvailableCopies()).isEqualTo(3);
    }

    @Test
    void addCopies_nonPositiveCount_throws() {
        assertThatThrownBy(() -> bookService.addCopies(1L, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getById_missing_throwsNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getById(99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void searchByQuery_blank_listsAll() {
        bookService.searchByQuery("  ");

        org.mockito.Mockito.verify(bookRepository).findAll();
    }

    @Test
    void searchByQuery_delegatesToRepository() {
        bookService.searchByQuery("Clean");

        org.mockito.Mockito.verify(bookRepository).searchByTitleAuthorOrIsbn("Clean");
    }
}
