package io.github.hrashk.books.api.books;

import io.github.hrashk.books.api.categories.Category;
import io.github.hrashk.books.api.categories.CategoryService;
import io.github.hrashk.books.api.common.BaseService;
import org.springframework.stereotype.Service;

@Service
public class BookService extends BaseService<Book, BookRepository> {
    private final CategoryService categoryService;

    public BookService(BookRepository repository, CategoryService categoryService) {
        super(repository, "Book");
        this.categoryService = categoryService;
    }

    @Override
    public Long add(Book book) {
        String categoryName = book.getCategory().getName();

        Category category = categoryService.getOrAdd(categoryName);
        book.setCategory(category);

        return super.add(book);
    }
}
