package io.github.hrashk.books.api.books.web;

import io.github.hrashk.books.api.books.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/books", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class BooksController {
    private final BookService service;
    private final BookMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable Long id) {
        BookResponse response = mapper.map(service.findById(id));

        return ResponseEntity.ok(response);
    }
}
