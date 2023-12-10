package io.github.hrashk.books.api.common;

public record CrudResult<ID>(Status status, ID id) {
    public static <ID> CrudResult<ID> found(ID id) {
        return new CrudResult<>(Status.FOUND, id);
    }

    public static <ID> CrudResult<ID> updated(ID newId) {
        return new CrudResult<>(Status.UPDATED, newId);
    }

    public static <ID> CrudResult<ID> created(ID newId) {
        return new CrudResult<>(Status.CREATED, newId);
    }

    public enum Status {
        UPDATED, CREATED, FOUND;
    }
}
