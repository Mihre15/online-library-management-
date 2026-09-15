package com.library_management.library_project.repository;

import com.library_management.library_project.entity.Book;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByCategoryIgnoreCase(String category);

    boolean existsByIsbn(String isbn);
}
