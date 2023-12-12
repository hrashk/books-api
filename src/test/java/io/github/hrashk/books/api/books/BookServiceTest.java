package io.github.hrashk.books.api.books;

import io.github.hrashk.books.api.CachingConfig;
import io.github.hrashk.books.api.categories.Category;
import io.github.hrashk.books.api.categories.CategoryService;
import io.github.hrashk.books.api.common.CrudResult;
import io.github.hrashk.books.api.util.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Import({BookService.class, CategoryService.class, CachingConfig.class})
class BookServiceTest extends ServiceTest {
    protected Cache cache;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private BookService service;

    @BeforeEach
    void setUpCache() {
        cache = cacheManager.getCache(BookService.BOOKS);
        assertThat(cache).as("Cache").isNotNull();
        cache.clear();
    }

    @Test
    void findByCategory() {
        Book book = seeder.books().get(0);
        String category = book.getCategory().getName();

        List<Book> books = service.findByCategory(category);
        assertThat(books).isNotEmpty();

        Cache.ValueWrapper wrapper = cache.get(category);
        assertThat(wrapper).as("Cached value").isNotNull();

        List<Book> cached = (List<Book>) wrapper.get();
        assertThat(cached).isNotEmpty();
        assertThat(cached).allSatisfy(b -> assertThat(b.getCategory().getName()).isEqualTo(category));
    }

    @Test
    void findByTitleAndAuthor() {
        Book book = seeder.books().get(0);
        Book foundBook = service.findByTitleAndAuthor(book.getTitle(), book.getAuthor());
        assertThat(foundBook).isEqualTo(book);

        SimpleKey key = new SimpleKey(book.getTitle(), book.getAuthor());
        Book cachedBook = cache.get(key, Book.class);
        assertThat(cachedBook).isEqualTo(book);
    }

    @Test
    void addNewBook() {
        Book book = seeder.books().get(0);
        String category = book.getCategory().getName();

        int originalSize = service.findByCategory(category).size();
        assertThat(cache.get(category)).as("Original cache").isNotNull();

        Book anotherBookWithSameCategory = seeder.aRandomBook().toBuilder().category(book.getCategory()).build();
        CrudResult<Long> result = service.add(anotherBookWithSameCategory);
        assertAll(
                () -> assertThat(result.status()).isEqualTo(CrudResult.Status.CREATED),
                () -> assertThat(service.findByCategory(category)).hasSize(originalSize + 1)
        );
    }

    @Test
    void addSimilarBook() {
        Book book = seeder.books().get(0);
        String category = book.getCategory().getName();

        int originalSize = service.findByCategory(category).size();
        assertThat(cache.get(category)).as("Original cache").isNotNull();

        String newCategory = "random-cat";
        Book similarBook = book.toBuilder()
                .category(new Category().toBuilder().name(newCategory).build())
                .build();
        CrudResult<Long> result = service.add(similarBook);

        assertAll(
                () -> assertThat(result.status()).isEqualTo(CrudResult.Status.FOUND),
                () -> assertThat(service.findByCategory(category)).hasSize(originalSize - 1),
                () -> assertThat(service.findByTitleAndAuthor(similarBook.getTitle(), similarBook.getAuthor()))
                        .isEqualTo(similarBook)
        );
    }
}
