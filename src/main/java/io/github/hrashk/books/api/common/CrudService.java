package io.github.hrashk.books.api.common;


import io.github.hrashk.books.api.exceptions.EntityNotFoundException;

public interface CrudService<E, ID> {
    E findById(ID id) throws EntityNotFoundException;

    ID updateOrAdd(ID id, E entity);

    ID add(E entity);

    void deleteById(ID id) throws EntityNotFoundException;
}
