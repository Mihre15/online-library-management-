package com.library_management.project.repository;

import com.library_management.project.entity.Book;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    /** Loads a book while holding a database write lock for inventory mutations. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByCategoryIgnoreCase(String category);

    boolean existsByIsbn(String isbn);

    @Query("""
            SELECT b FROM Book b
            WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    List<Book> searchByTitleAuthorOrIsbn(@Param("query") String query);
}
