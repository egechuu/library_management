package com.example.library.integration;

import com.example.library.model.Book;
import com.example.library.model.Genre;
import com.example.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * INTEGRATION TEST - Repository Layer
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    private Book createBook(String isbn, String title, String author, int copies, Genre genre) {
        Book book = new Book(isbn, title, author, copies, genre);
        book.setPublishedDate(LocalDate.of(2020, 1, 1));
        return bookRepository.save(book);
    }

    // =========================================================================
    // EXAMPLE: Basic CRUD and custom query tests — filled in
    // =========================================================================

    @Nested
    @DisplayName("Basic CRUD operations")
    class CrudTests {

        @Test
        @DisplayName("should save and retrieve a book by ID")
        void shouldSaveAndFindById() {
            Book saved = createBook("978-0-13-468599-1", "Clean Code", "Robert C. Martin", 3, Genre.TECHNOLOGY);

            Optional<Book> found = bookRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getTitle()).isEqualTo("Clean Code");
            assertThat(found.get().getIsbn()).isEqualTo("978-0-13-468599-1");
        }

        @Test
        @DisplayName("should find book by ISBN")
        void shouldFindByIsbn() {
            createBook("978-0-13-468599-1", "Clean Code", "Robert C. Martin", 3, Genre.TECHNOLOGY);

            Optional<Book> found = bookRepository.findByIsbn("978-0-13-468599-1");

            assertThat(found).isPresent();
            assertThat(found.get().getTitle()).isEqualTo("Clean Code");
        }

        @Test
        @DisplayName("should return empty when ISBN not found")
        void shouldReturnEmpty_WhenIsbnNotFound() {
            Optional<Book> found = bookRepository.findByIsbn("non-existent");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Custom query methods")
    class CustomQueryTests {

        @Test
        @DisplayName("should search books by keyword in title or author (case insensitive)")
        void shouldSearchByKeyword() {
            createBook("978-1", "Clean Code", "Robert C. Martin", 3, Genre.TECHNOLOGY);
            createBook("978-2", "Clean Architecture", "Robert C. Martin", 2, Genre.TECHNOLOGY);
            createBook("978-3", "Design Patterns", "Gang of Four", 5, Genre.TECHNOLOGY);

            List<Book> results = bookRepository.searchBooks("clean");

            assertThat(results).hasSize(2);
            assertThat(results).extracting(Book::getTitle)
                    .containsExactlyInAnyOrder("Clean Code", "Clean Architecture");
        }

        @Test
        @DisplayName("should find available books (copies > 0)")
        void shouldFindAvailableBooks() {
            Book available = createBook("978-1", "Available Book", "Author A", 3, Genre.FICTION);
            Book unavailable = createBook("978-2", "Unavailable Book", "Author B", 1, Genre.FICTION);
            unavailable.setAvailableCopies(0);
            bookRepository.save(unavailable);

            List<Book> results = bookRepository.findAvailableBooks();

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getTitle()).isEqualTo("Available Book");
        }
    }

    // =========================================================================
    // TODO: Students should write these integration tests
    // =========================================================================

    @Nested
    @DisplayName("Genre and author queries")
    class FilterTests {

        @Test
        @DisplayName("should find books by genre")
        void shouldFindByGenre() {
            createBook("978-1", "Cosmos", "Carl Sagan", 2, Genre.SCIENCE);
            createBook("978-2", "Dune", "Frank Herbert", 3, Genre.FICTION);
            createBook("978-3", "A Brief History", "Stephen Hawking", 1, Genre.SCIENCE);

            List<Book> result = bookRepository.findByGenre(Genre.SCIENCE);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Book::getGenre).containsOnly(Genre.SCIENCE);
        }

        @Test
        @DisplayName("should find books by author (case insensitive, partial match)")
        void shouldFindByAuthor() {
            createBook("978-1", "Clean Code", "Robert C. Martin", 3, Genre.TECHNOLOGY);
            createBook("978-2", "Clean Architecture", "Robert C. Martin", 2, Genre.TECHNOLOGY);
            createBook("978-3", "Dune", "Frank Herbert", 3, Genre.FICTION);

            List<Book> result = bookRepository.findByAuthorContainingIgnoreCase("robert");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Book::getAuthor)
                    .allMatch(a -> a.toLowerCase().contains("robert"));
        }

        @Test
        @DisplayName("should search by author name using searchBooks()")
        void shouldSearchByAuthorKeyword() {
            createBook("978-1", "Clean Code", "Robert C. Martin", 3, Genre.TECHNOLOGY);
            createBook("978-2", "Dune", "Frank Herbert", 3, Genre.FICTION);

            List<Book> result = bookRepository.searchBooks("Martin");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAuthor()).contains("Martin");
        }

        @Test
        @DisplayName("should return empty list when no books match search")
        void shouldReturnEmpty_WhenNoMatch() {
            createBook("978-1", "Clean Code", "Robert C. Martin", 3, Genre.TECHNOLOGY);

            List<Book> result = bookRepository.searchBooks("xyznomatch123");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should enforce unique ISBN constraint")
        void shouldEnforceUniqueIsbn() {
            Book book1 = new Book("978-SAME", "Book One", "Author A", 1, Genre.FICTION);
            book1.setPublishedDate(java.time.LocalDate.of(2020, 1, 1));
            bookRepository.saveAndFlush(book1);

            Book book2 = new Book("978-SAME", "Book Two", "Author B", 2, Genre.SCIENCE);
            book2.setPublishedDate(java.time.LocalDate.of(2020, 1, 1));

            assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                    () -> bookRepository.saveAndFlush(book2));
        }

        @Test
        @DisplayName("should handle deleting a book")
        void shouldDeleteBook() {
            Book saved = createBook("978-1", "To Delete", "Author", 1, Genre.FICTION);
            Long id = saved.getId();

            bookRepository.deleteById(id);

            assertThat(bookRepository.findById(id)).isEmpty();
        }
    }
}
