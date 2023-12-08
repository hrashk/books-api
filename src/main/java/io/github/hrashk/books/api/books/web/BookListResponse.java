package io.github.hrashk.books.api.books.web;

import java.util.List;

public record BookListResponse(List<BookResponse> books) {
}
