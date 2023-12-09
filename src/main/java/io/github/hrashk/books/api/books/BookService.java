package io.github.hrashk.books.api.books;

import io.github.hrashk.books.api.categories.Category;
import io.github.hrashk.books.api.categories.CategoryService;
import io.github.hrashk.books.api.common.CrudService;
import io.github.hrashk.books.api.exceptions.EntityNotFoundException;
import io.github.hrashk.books.api.util.BeanCopyUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Transactional
    @Override
    public Long add(Book book) {
        getOrAddCategory(book);

        return repository.save(book).getId();
    }

    @Transactional
    @Override
    public Long updateOrAdd(Long id, Book book) {
        if (!repository.existsById(id)) {
            return add(book);
        }

        getOrAddCategory(book);
        Book current = findById(id);
        BeanCopyUtils.copyProperties(book, current);

        return repository.save(current).getId();
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
