package io.github.hrashk.books.api.common;

public record CrudResult<ID>(Status status, ID id) {
    public static CrudResult<Long> found(Long id) {
        return new CrudResult<>(Status.FOUND, id);
    }

    public static CrudResult<Long> updated(Long newId) {
        return new CrudResult<>(Status.UPDATED, newId);
    }

    public static CrudResult<Long> created(Long newId) {
        return new CrudResult<>(Status.CREATED, newId);
    }

    public enum Status {
        UPDATED, CREATED, FOUND;
    }
}
