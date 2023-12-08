package io.github.hrashk.books.api.books.web;

import io.github.hrashk.books.api.books.Book;
import io.github.hrashk.books.api.books.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/v1/books", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class BooksController {
    private final BookService service;
    private final BookMapper mapper;

    @GetMapping
    public ResponseEntity<BookListResponse> findByCategory(@RequestParam String category) {
        List<Book> books = service.findByCategory(category);

        BookListResponse response = mapper.wrap(books);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable Long id) {
        BookResponse response = mapper.map(service.findById(id));

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<BookResponse> add(@RequestBody @Valid UpsertRequest request) {
        Long id = service.add(mapper.map(request));

        BookResponse response = mapper.map(service.findById(id));

        return created(id, response);
    }

    private static ResponseEntity<BookResponse> created(Long id, BookResponse response) {
        return ResponseEntity.created(URI.create("/" + id)).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(@PathVariable Long id, @RequestBody @Valid UpsertRequest request) {
        Long newId = service.updateOrAdd(id, mapper.map(request));

        BookResponse response = mapper.map(service.findById(newId));

        if (Objects.equals(newId, id))
            return ResponseEntity.ok(response);
        else
            return created(newId, response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
