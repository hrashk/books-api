package io.github.hrashk.books.api.books.web;

import io.github.hrashk.books.api.books.Book;
import io.github.hrashk.books.api.books.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Add a new book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "A new book was added"),
            @ApiResponse(responseCode = "302", description = "A book was found with the same title and author." +
                    " It was updated instead of creating a new one"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @PostMapping
    public ResponseEntity<BookResponse> add(@RequestBody @Valid UpsertRequest request) {
        var result = service.add(mapper.map(request));

        return mapper.map(result);
    }


    @Operation(summary = "Update a book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The book was updated"),
            @ApiResponse(responseCode = "201", description = "There was no book with the provided id," +
                    " so a new book was added"),
            @ApiResponse(responseCode = "302", description = "A book was found with the same title and author." +
                    " It was updated instead of that with the provided id. The original book was deleted.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(@PathVariable Long id, @RequestBody @Valid UpsertRequest request) {
        var result = service.update(id, mapper.map(request));

        return mapper.map(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
