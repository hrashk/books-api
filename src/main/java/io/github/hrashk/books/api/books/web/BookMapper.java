package io.github.hrashk.books.api.books.web;

import io.github.hrashk.books.api.books.Book;
import io.github.hrashk.books.api.categories.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class BookMapper {
    @Mapping(target = "category", source = "category.name")
    public abstract BookResponse map(Book book);

    public abstract Category map(String name);

    public abstract Book map(UpsertRequest request);
}
