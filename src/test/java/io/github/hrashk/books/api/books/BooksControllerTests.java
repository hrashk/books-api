package io.github.hrashk.books.api.books;

import io.github.hrashk.books.api.books.web.BookResponse;
import io.github.hrashk.books.api.util.ControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class BooksControllerTests extends ControllerTest {
    private static final String BOOKS_URL = "/api/v1/books";
    private static final String BOOKS_ID_URL = BOOKS_URL + "/{id}";

    @Test
    void findById() {
        Long bookId = seeder.books().get(0).getId();

        ResponseEntity<BookResponse> response = rest.getForEntity(BOOKS_ID_URL, BookResponse.class, bookId);

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).hasNoNullFieldsOrProperties(),
                () -> assertThat(response.getBody().id()).isEqualTo(bookId)
        );
    }
}
