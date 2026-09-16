package com.library_management.library_project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.library_management.library_project.entity.Book;
import com.library_management.library_project.exception.NotFoundException;
import com.library_management.library_project.repository.BookRepository;
import java.util.List;
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
    void listAll_returnsEveryBook() {
        Book book = Book.builder().id(1L).title("Clean Code").author("Robert Martin")
                .isbn("isbn-1").category("Software").totalCopies(1).availableCopies(1).build();
        when(bookRepository.findAll()).thenReturn(List.of(book));

        assertThat(bookService.listAll()).containsExactly(book);
    }

    @Test
    void searchByCategory_delegatesToRepository() {
        Book book = Book.builder().id(1L).title("Clean Code").author("Robert Martin")
                .isbn("isbn-1").category("Software").totalCopies(1).availableCopies(1).build();
        when(bookRepository.findByCategoryIgnoreCase("software")).thenReturn(List.of(book));

        assertThat(bookService.searchByCategory("software")).containsExactly(book);
    }

    // ---- updateBook ---------------------------------------------------

    private Book existingBook() {
        return Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert Martin")
                .isbn("isbn-1")
                .category("Software")
                .totalCopies(3)
                .availableCopies(3)
                .build();
    }

    @Test
    void updateBook_isbnUnchanged_updatesWithoutCheckingUniqueness() {
        Book book = existingBook();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.updateBook(1L, "Clean Code (2nd Ed.)", "Robert C. Martin", "isbn-1", "Craft");

        assertThat(result.getTitle()).isEqualTo("Clean Code (2nd Ed.)");
        assertThat(result.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(result.getCategory()).isEqualTo("Craft");
        verify(bookRepository, never()).existsByIsbn(any());
    }

    @Test
    void updateBook_newIsbnAlreadyTaken_throws() {
        Book book = existingBook();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbn("isbn-2")).thenReturn(true);

        assertThatThrownBy(() -> bookService.updateBook(1L, "Clean Code", "Robert Martin", "isbn-2", "Software"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateBook_newIsbnAvailable_updatesSuccessfully() {
        Book book = existingBook();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbn("isbn-2")).thenReturn(false);

        Book result = bookService.updateBook(1L, "Clean Code", "Robert Martin", "isbn-2", "Software");

        assertThat(result.getIsbn()).isEqualTo("isbn-2");
    }

    @Test
    void updateBook_missingBook_throwsNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(99L, "Title", "Author", "isbn-x", "Category"))
                .isInstanceOf(NotFoundException.class);
    }

    // ---- deleteBook -----------------------------------------------------

    @Test
    void deleteBook_existingBook_deletesIt() {
        Book book = existingBook();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.deleteBook(1L);

        verify(bookRepository).delete(book);
    }

    @Test
    void deleteBook_missingBook_throwsNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteBook(99L)).isInstanceOf(NotFoundException.class);
    }

    // ---- searchByTitleOrAuthorOrIsbn --------------------------------------

    @Test
    void searchByTitleOrAuthorOrIsbn_matchesByTitleAuthorOrIsbn_excludesNonMatches() {
        Book matchesTitle = Book.builder().id(1L).title("Clean Code").author("Robert Martin")
                .isbn("111").category("Software").totalCopies(1).availableCopies(1).build();
        Book matchesAuthor = Book.builder().id(2L).title("1984").author("George Cleanwell")
                .isbn("222").category("Fiction").totalCopies(1).availableCopies(1).build();
        Book matchesIsbn = Book.builder().id(3L).title("The Hobbit").author("J.R.R. Tolkien")
                .isbn("clean-333").category("Fiction").totalCopies(1).availableCopies(1).build();
        Book noMatch = Book.builder().id(4L).title("The Republic").author("Plato")
                .isbn("444").category("Classics").totalCopies(1).availableCopies(1).build();
        when(bookRepository.findAll()).thenReturn(List.of(matchesTitle, matchesAuthor, matchesIsbn, noMatch));

        List<Book> result = bookService.searchByTitleOrAuthorOrIsbn("clean");

        assertThat(result).containsExactlyInAnyOrder(matchesTitle, matchesAuthor, matchesIsbn);
    }

    @Test
    void searchByTitleOrAuthorOrIsbn_isCaseInsensitiveForTitleAndAuthor() {
        Book book = Book.builder().id(1L).title("Clean Code").author("Robert Martin")
                .isbn("isbn-1").category("Software").totalCopies(1).availableCopies(1).build();
        when(bookRepository.findAll()).thenReturn(List.of(book));

        assertThat(bookService.searchByTitleOrAuthorOrIsbn("CLEAN")).containsExactly(book);
        assertThat(bookService.searchByTitleOrAuthorOrIsbn("MARTIN")).containsExactly(book);
    }

    @Test
    void searchByTitleOrAuthorOrIsbn_noMatches_returnsEmptyList() {
        Book book = Book.builder().id(1L).title("Clean Code").author("Robert Martin")
                .isbn("isbn-1").category("Software").totalCopies(1).availableCopies(1).build();
        when(bookRepository.findAll()).thenReturn(List.of(book));

        assertThat(bookService.searchByTitleOrAuthorOrIsbn("nonexistent")).isEmpty();
    }
}
