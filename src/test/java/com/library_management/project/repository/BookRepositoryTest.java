package com.library_management.project.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.library_management.project.entity.Book;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class BookRepositoryTest {

    @Autowired private BookRepository bookRepository;

    private Book book(String isbn, String category) {
        return Book.builder()
                .title("Clean Code")
                .author("Robert Martin")
                .isbn(isbn)
                .category(category)
                .totalCopies(3)
                .availableCopies(3)
                .build();
    }

    @Test
    void findByIsbn_returnsSavedBook() {
        bookRepository.save(book("isbn-1", "Software"));

        assertThat(bookRepository.findByIsbn("isbn-1")).isPresent();
    }

    @Test
    void findByCategoryIgnoreCase_matchesRegardlessOfCase() {
        bookRepository.save(book("isbn-1", "Software"));
        bookRepository.save(book("isbn-2", "Fiction"));

        List<Book> results = bookRepository.findByCategoryIgnoreCase("software");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getIsbn()).isEqualTo("isbn-1");
    }

    @Test
    void existsByIsbn_trueOnlyAfterSaving() {
        assertThat(bookRepository.existsByIsbn("isbn-1")).isFalse();

        bookRepository.save(book("isbn-1", "Software"));

        assertThat(bookRepository.existsByIsbn("isbn-1")).isTrue();
    }

    @Test
    void searchByTitleAuthorOrIsbn_matchesAnyField() {
        bookRepository.save(book("isbn-1", "Software"));
        Book other = Book.builder()
                .title("Domain-Driven Design")
                .author("Eric Evans")
                .isbn("isbn-2")
                .category("Software")
                .totalCopies(1)
                .availableCopies(1)
                .build();
        bookRepository.save(other);

        assertThat(bookRepository.searchByTitleAuthorOrIsbn("evans")).hasSize(1);
        assertThat(bookRepository.searchByTitleAuthorOrIsbn("isbn-1")).hasSize(1);
        assertThat(bookRepository.searchByTitleAuthorOrIsbn("clean")).hasSize(1);
        assertThat(bookRepository.searchByTitleAuthorOrIsbn("missing")).isEmpty();
    }
}
