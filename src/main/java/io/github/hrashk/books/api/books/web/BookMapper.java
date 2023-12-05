package io.github.hrashk.books.api.books.web;

import io.github.hrashk.books.api.books.Book;
import io.github.hrashk.books.api.books.BookService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class BookMapper {
    @Autowired
    protected BookService service;

    @Mapping(target = "category", source = "category.name")
    public abstract BookResponse map(Book book);
}
