package io.github.hrashk.books.api.books;

import io.github.hrashk.books.api.common.BaseService;
import org.springframework.stereotype.Service;

@Service
public class BookService extends BaseService<Book, BookRepository> {
    protected BookService(BookRepository repository) {
        super(repository, "Book");
    }
}
