package io.github.hrashk.books.api.common;

import io.github.hrashk.books.api.exceptions.EntityNotFoundException;
import io.github.hrashk.books.api.util.BeanCopyUtils;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class BaseService<E extends BaseEntity, R extends JpaRepository<E, Long>> implements CrudService<E, Long> {
    protected final R repository;
    protected final String entityName;

    protected BaseService(R repository, String entityName) {
        this.repository = repository;
        this.entityName = entityName;
    }

    @Override
    public E findById(Long id) throws EntityNotFoundException {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(entityName, id));
    }

    @Override
    @Transactional
    public Long updateOrAdd(Long id, E entity) {
        try {
            var current = findById(id);
            BeanCopyUtils.copyProperties(entity, current);

            return repository.save(current).getId();
        } catch (EntityNotFoundException ex) {
            return add(entity);
        }
    }

    @Override
    @Transactional
    public Long add(E entity) {
        return repository.save(entity).getId();
    }

    @Override
    @Transactional
    public void deleteById(Long id) throws EntityNotFoundException {
        repository.delete(findById(id));
    }
}
