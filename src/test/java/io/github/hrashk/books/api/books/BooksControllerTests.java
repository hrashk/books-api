package io.github.hrashk.books.api.books;

import io.github.hrashk.books.api.books.web.BookListResponse;
import io.github.hrashk.books.api.books.web.BookResponse;
import io.github.hrashk.books.api.books.web.UpsertRequest;
import io.github.hrashk.books.api.exceptions.ErrorInfo;
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
    void findAll() {
        ResponseEntity<BookListResponse> response = rest.getForEntity(BOOKS_URL, BookListResponse.class);

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().books()).hasSize(seeder.books().size())
        );
    }

    @Test
    void findByCategory() {
        String category = seeder.books().get(0).getCategory().getName();

        ResponseEntity<BookListResponse> response = rest.getForEntity(BOOKS_URL + "/by-category?category={c}",
                BookListResponse.class, category);

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().books()).isNotEmpty(),
                () -> assertThat(response.getBody().books()).allSatisfy(b ->
                        assertThat(b.category()).isEqualTo(category))
        );
    }

    @Test
    void findByTitleAndAuthor() {
        var book = seeder.books().get(0);

        ResponseEntity<BookResponse> response = rest.getForEntity(BOOKS_URL
                        + "/by-title-and-author?title={t}&author={a}",
                BookResponse.class, book.getTitle(), book.getAuthor());

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).hasNoNullFieldsOrProperties(),
                () -> assertThat(response.getBody().title()).isEqualTo(book.getTitle()),
                () -> assertThat(response.getBody().author()).isEqualTo(book.getAuthor())
        );
    }

    @Test
    void findMissingByTitleAndAuthor() {
        ResponseEntity<ErrorInfo> response = rest.getForEntity(BOOKS_URL
                        + "/by-title-and-author?title={t}&author={a}",
                ErrorInfo.class, "ttt", "aaa");

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND),
                () -> assertThat(response.getBody().message()).contains("book")
        );
    }

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

    @Test
    void findMissing() {
        Long bookId = INVALID_ID;

        ResponseEntity<ErrorInfo> response = rest.getForEntity(BOOKS_ID_URL, ErrorInfo.class, bookId);

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND),
                () -> assertThat(response.getBody().message()).contains("Book")
        );
    }

    @Test
    void add() {
        String existingCategory = seeder.categories().get(4).getName();
        addBookWithCategory(existingCategory);
    }

    @Test
    void addWithNewCategory() {
        String newCategory = "random-cat";
        addBookWithCategory(newCategory);
    }

    private void addBookWithCategory(String categoryName) {
        UpsertRequest request = new UpsertRequest("t", "a", categoryName);

        ResponseEntity<BookResponse> response = rest.postForEntity(BOOKS_URL, request, BookResponse.class);

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                () -> assertThat(response.getBody()).hasNoNullFieldsOrProperties(),
                () -> assertThat(response.getBody().author()).isEqualTo("a"),
                () -> assertThat(response.getBody().category()).isEqualTo(categoryName),
                () -> assertThat(response.getBody().title()).isEqualTo("t")
        );
    }

    @Test
    void addExistingBookWithNewCategory() {
        String newCategory = "random-cat";
        var book = seeder.books().get(0);
        UpsertRequest request = new UpsertRequest(book.getTitle(), book.getAuthor(), newCategory);

        ResponseEntity<BookResponse> response = rest.postForEntity(BOOKS_URL, request, BookResponse.class);

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND),
                () -> assertThat(response.getBody()).hasNoNullFieldsOrProperties(),
                () -> assertThat(response.getBody().category()).isEqualTo(newCategory),
                () -> assertThat(response.getBody().id()).isEqualTo(book.getId())
        );

        ResponseEntity<BookResponse> findResponse = rest.getForEntity(BOOKS_URL
                        + "/by-title-and-author?title={t}&author={a}",
                BookResponse.class, book.getTitle(), book.getAuthor());

        assertAll(
                () -> assertThat(findResponse.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(findResponse.getBody()).hasNoNullFieldsOrProperties(),
                () -> assertThat(response.getBody().category()).isEqualTo(newCategory),
                () -> assertThat(response.getBody().id()).isEqualTo(book.getId())
        );
    }

    @Test
    void addBroken() {
        UpsertRequest request = new UpsertRequest("", "  ", null);

        ResponseEntity<ErrorInfo> response = rest.postForEntity(BOOKS_URL, request, ErrorInfo.class);

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                () -> assertThat(response.getBody().message()).contains("title", "author", "category")
        );
    }

    @Test
    void updateAuthor() {
        var book = seeder.books().get(0);
        UpsertRequest request = new UpsertRequest(book.getTitle(), "asdf", book.getCategory().getName());

        ResponseEntity<BookResponse> response = put(BOOKS_ID_URL, request, BookResponse.class, book.getId());

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).hasNoNullFieldsOrProperties(),
                () -> assertThat(response.getBody().author()).isEqualTo("asdf")
        );
    }

    @Test
    void updateCategory() {
        var book = seeder.books().get(0);
        String category = seeder.aRandomCategory().getName();
        UpsertRequest request = new UpsertRequest(book.getTitle(), book.getAuthor(), category);

        ResponseEntity<BookResponse> response = put(BOOKS_ID_URL, request, BookResponse.class, book.getId());

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).hasNoNullFieldsOrProperties(),
                () -> assertThat(response.getBody().category()).isEqualTo(category)
        );
    }

    @Test
    void transformBookIntoAnother() {
        String newCategory = "random-cat";
        var book1 = seeder.books().get(0);
        var book2 = seeder.books().get(1);
        UpsertRequest request = new UpsertRequest(book1.getTitle(), book1.getAuthor(), newCategory);

        ResponseEntity<BookResponse> response = put(BOOKS_ID_URL, request, BookResponse.class, book2.getId());

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND),
                () -> assertThat(response.getBody()).hasNoNullFieldsOrProperties(),
                () -> assertThat(response.getBody().category()).isEqualTo(newCategory),
                () -> assertThat(response.getBody().id()).isEqualTo(book1.getId())
        );

        ResponseEntity<ErrorInfo> findResponse = rest.getForEntity(BOOKS_ID_URL, ErrorInfo.class, book2.getId());

        assertAll(
                () -> assertThat(findResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND),
                () -> assertThat(findResponse.getBody().message()).contains("Book")
        );
    }

    @Test
    void updateMissing() {
        UpsertRequest request = new UpsertRequest("t", "a", "c");

        ResponseEntity<BookResponse> response = put(BOOKS_ID_URL, request, BookResponse.class, INVALID_ID);

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                () -> assertThat(response.getBody()).hasNoNullFieldsOrProperties()
        );
    }

    @Test
    void delete() {
        Long bookId = seeder.books().get(0).getId();

        ResponseEntity<Void> response = delete(BOOKS_ID_URL, bookId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<ErrorInfo> findResponse = rest.getForEntity(BOOKS_ID_URL, ErrorInfo.class, bookId);
        assertAll(
                () -> assertThat(findResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND),
                () -> assertThat(findResponse.getBody().message()).contains("Book")
        );
    }

    @Test
    void deleteMissing() {
        Long bookId = INVALID_ID;

        ResponseEntity<ErrorInfo> response = delete(BOOKS_ID_URL, ErrorInfo.class, bookId);

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND),
                () -> assertThat(response.getBody().message()).contains("Book")
        );
    }
}
