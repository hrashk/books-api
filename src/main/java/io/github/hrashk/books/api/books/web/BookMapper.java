package io.github.hrashk.books.api.books.web;

import io.github.hrashk.books.api.books.Book;
import io.github.hrashk.books.api.books.BookService;
import io.github.hrashk.books.api.categories.Category;
import io.github.hrashk.books.api.common.CrudResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class BookMapper {
    @Autowired
    protected BookService service;

    @Mapping(target = "category", source = "category.name")
    public abstract BookResponse map(Book book);

    public abstract Category map(String name);

    public abstract Book map(UpsertRequest request);

    public abstract List<BookResponse> map(List<Book> books);

    public BookListResponse wrap(List<Book> books) {
        return new BookListResponse(map(books));
    }

    public ResponseEntity<BookResponse> map(CrudResult<Long> result) {
        BookResponse response = map(service.findById(result.id()));

        return switch (result.status()) {
            case UPDATED -> ResponseEntity.ok(response);
            case CREATED -> ResponseEntity.created(URI.create("/" + response.id())).body(response);
            case FOUND -> ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/" + response.id())).body(response);
        };
    }
}
