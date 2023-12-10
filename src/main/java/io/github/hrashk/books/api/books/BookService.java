package io.github.hrashk.books.api.books;

import io.github.hrashk.books.api.categories.Category;
import io.github.hrashk.books.api.categories.CategoryService;
import io.github.hrashk.books.api.common.CrudResult;
import io.github.hrashk.books.api.common.CrudService;
import io.github.hrashk.books.api.exceptions.EntityNotFoundException;
import io.github.hrashk.books.api.util.BeanCopyUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class BookService implements CrudService<Book, Long> {
    private final BookRepository repository;
    private final CategoryService categoryService;

    public BookService(BookRepository repository, CategoryService categoryService) {
        this.repository = repository;
        this.categoryService = categoryService;
    }

    public List<Book> findAll() {
        return repository.findAll();
    }

    public List<Book> findByCategory(String category) {
        return repository.findByCategoryName(category);
    }

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
            return CrudResult.found(saveCopy(book, byProperties.get()));
        }
    }

    private Long save(Book book) {
        return repository.save(book).getId();
    }

    private Long saveCopy(Book from, Book into) {
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
