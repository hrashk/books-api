package io.github.hrashk.books.api.books.web;

import jakarta.validation.constraints.NotBlank;

public record UpsertRequest(
        @NotBlank String title,
        @NotBlank String author,
        @NotBlank String category) {
}
