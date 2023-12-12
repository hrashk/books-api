package io.github.hrashk.books.api.books;

import io.github.hrashk.books.api.categories.Category;
import io.github.hrashk.books.api.categories.CategoryService;
import io.github.hrashk.books.api.common.CrudResult;
import io.github.hrashk.books.api.common.CrudService;
import io.github.hrashk.books.api.exceptions.EntityNotFoundException;
import io.github.hrashk.books.api.util.BeanCopyUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookService implements CrudService<Book, Long> {
    static final String BOOKS = "books";
    private final BookRepository repository;
    private final CategoryService categoryService;
    private final CacheManager cacheManager;

    public List<Book> findAll() {
        return repository.findAll();
    }

    @Cacheable(BOOKS)
    public List<Book> findByCategory(String category) {
        return repository.findByCategoryName(category);
    }

    @Cacheable(BOOKS)
    public Book findByTitleAndAuthor(String title, String author) {
        return repository.findByTitleAndAuthor(title, author)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No book found with title '%s' by %s".formatted(title, author)));
    }

    /**
     * @return <ul>
     * <li>{@link CrudResult.Status#CREATED} if a new book was created.</li>
     * <li>{@link CrudResult.Status#FOUND} if another book was found with the same title and author.</li>
     * </ul>
     */
    @Transactional
    @Override
    public CrudResult<Long> add(Book book) {
        getOrAddCategory(book);

        return repository.findByTitleAndAuthor(book.getTitle(), book.getAuthor())
                .map(b -> CrudResult.found(saveCopy(book, b)))
                .orElseGet(() -> CrudResult.created(save(book)));
    }

    /**
     * @return <ul>
     * <li>{@link CrudResult.Status#UPDATED} if the book was updated.</li>
     * <li>{@link CrudResult.Status#FOUND} if another book is found with the same title and author.
     * The book with the provided id is deleted in this case.</li>
     * <li>{@link CrudResult.Status#CREATED} if there was no book with specified id or attributes.</li>
     */
    @Transactional
    @Override
    public CrudResult<Long> update(Long id, Book book) {
        var byId = repository.findById(id);

        if (byId.isEmpty()) {
            return add(book);
        }

        getOrAddCategory(book);
        var byProperties = repository.findByTitleAndAuthor(book.getTitle(), book.getAuthor());

        if (byProperties.isEmpty() || Objects.equals(byProperties.get().getId(), id)) {
            return CrudResult.updated(saveCopy(book, byId.get()));
        } else {
            repository.delete(byId.get());
            return CrudResult.found(saveCopy(book, byProperties.get()));
        }
    }

    private Long save(Book book) {
        cacheManager.getCache(BOOKS).evict(book.getCategory().getName());

        return repository.save(book).getId();
    }

    private Long saveCopy(Book from, Book into) {
        cacheManager.getCache(BOOKS).evict(into.getCategory().getName());

        BeanCopyUtils.copyProperties(from, into);
        return repository.save(into).getId();
    }

    private void getOrAddCategory(Book book) {
        String categoryName = book.getCategory().getName();

        Category category = categoryService.getOrAdd(categoryName);

        book.setCategory(category);
    }

    @Override
    public Book findById(Long id) throws EntityNotFoundException {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book", id));
    }

    @Override
    @Transactional
    public void deleteById(Long id) throws EntityNotFoundException {
        repository.delete(findById(id));
    }
}
