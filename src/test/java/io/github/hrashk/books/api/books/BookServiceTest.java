package io.github.hrashk.books.api.books;

import io.github.hrashk.books.api.CachingConfig;
import io.github.hrashk.books.api.categories.CategoryService;
import io.github.hrashk.books.api.util.ServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import({BookService.class, CategoryService.class, CachingConfig.class})
class BookServiceTest extends ServiceTest {
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private BookService service;

    @Test
    void findByCategory() {
        Cache cache = cacheManager.getCache("books");
        assertThat(cache).as("Cache").isNotNull();

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
        Cache cache = cacheManager.getCache("books");
        assertThat(cache).as("Cache").isNotNull();

        Book book = seeder.books().get(0);
        Book foundBook = service.findByTitleAndAuthor(book.getTitle(), book.getAuthor());
        assertThat(foundBook).isEqualTo(book);

        SimpleKey key = new SimpleKey(book.getTitle(), book.getAuthor());
        Book cachedBook = cache.get(key, Book.class);
        assertThat(cachedBook).isEqualTo(book);
    }
}
