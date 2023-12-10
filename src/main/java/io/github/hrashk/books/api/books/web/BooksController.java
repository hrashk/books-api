package io.github.hrashk.books.api.books.web;

import io.github.hrashk.books.api.books.Book;
import io.github.hrashk.books.api.books.BookService;
import io.github.hrashk.books.api.common.CrudResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/books", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class BooksController {
    private final BookService service;
    private final BookMapper mapper;

    @GetMapping
    public ResponseEntity<BookListResponse> findAll() {
        List<Book> books = service.findAll();

        BookListResponse response = mapper.wrap(books);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-category")
    public ResponseEntity<BookListResponse> findByCategory(@RequestParam @NotBlank String category) {
        List<Book> books = service.findByCategory(category);

        BookListResponse response = mapper.wrap(books);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-title-and-author")
    public ResponseEntity<BookResponse> findByTitleAndAuthor(
            @RequestParam @NotBlank String title, @RequestParam @NotBlank String author) {
        Book book = service.findByTitleAndAuthor(title, author);

        BookResponse response = mapper.map(book);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable Long id) {
        BookResponse response = mapper.map(service.findById(id));

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<BookResponse> add(@RequestBody @Valid UpsertRequest request) {
        var result = service.add(mapper.map(request));

        return map(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(@PathVariable Long id, @RequestBody @Valid UpsertRequest request) {
        var result = service.update(id, mapper.map(request));

        return map(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<BookResponse> map(CrudResult<Long> result) {
        BookResponse response = mapper.map(service.findById(result.id()));

        return switch (result.status()) {
            case UPDATED -> ResponseEntity.ok(response);
            case CREATED -> ResponseEntity.created(URI.create("/" + response.id())).body(response);
            case FOUND -> ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/" + response.id())).body(response);
        };
    }

}
