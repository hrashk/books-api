package io.github.hrashk.books.api.books;

import io.github.hrashk.books.api.CachingConfig;
import io.github.hrashk.books.api.categories.Category;
import io.github.hrashk.books.api.categories.CategoryService;
import io.github.hrashk.books.api.common.CrudResult;
import io.github.hrashk.books.api.exceptions.EntityNotFoundException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        Category category = seeder.categories().get(0);
        String categoryName = category.getName();
        String author = "new author";
        String title = "new title";

        // cache things
        int size = service.findByCategory(categoryName).size();
        assertThatThrownBy(() -> service.findByTitleAndAuthor(title, author))
                .isInstanceOf(EntityNotFoundException.class);

        // modify
        Book book = new Book().toBuilder()
                .title(title)
                .author(author)
                .category(category)
                .build();
        CrudResult<Long> result = service.add(book);

        // check the caches are modified
        assertAll(
                () -> assertThat(result.status()).isEqualTo(CrudResult.Status.CREATED),
                () -> assertThat(service.findByCategory(categoryName)).hasSize(size + 1),
                () -> assertThat(service.findByTitleAndAuthor(title, author)).isEqualTo(book)
        );
    }

    @Test
    void addSimilarBook() {
        Book book = seeder.detachedBookCopy(0);
        String categoryName1 = book.getCategory().getName();

        Category category2 = seeder.aDifferentCategoryFrom(categoryName1);
        String categoryName2 = category2.getName();

        // cache things
        int size1 = service.findByCategory(categoryName1).size();
        int size2 = service.findByCategory(categoryName2).size();
        service.findByTitleAndAuthor(book.getTitle(), book.getAuthor());

        // modify
        Book similarBook = book.toBuilder().category(category2).build();
        CrudResult<Long> result = service.add(similarBook);

        // check the caches are modified
        assertAll(
                () -> assertThat(result.status()).isEqualTo(CrudResult.Status.FOUND),
                () -> assertThat(service.findByCategory(categoryName1)).hasSize(size1 - 1),
                () -> assertThat(service.findByCategory(categoryName2)).hasSize(size2 + 1),
                () -> assertThat(service.findByTitleAndAuthor(book.getTitle(), book.getAuthor()))
                        .isEqualTo(similarBook)
        );
    }

    @Test
    void updateAuthor() {
        Book book = seeder.detachedBookCopy(0);
        String categoryName = book.getCategory().getName();
        String newAuthor = "new author";

        // cache things
        int size = service.findByCategory(categoryName).size();
        service.findByTitleAndAuthor(book.getTitle(), book.getAuthor());
        assertThatThrownBy(() -> service.findByTitleAndAuthor(book.getTitle(), newAuthor))
                .isInstanceOf(EntityNotFoundException.class);

        // modify
        Book modifiedBook = book.toBuilder().author(newAuthor).build();
        CrudResult<Long> result = service.update(book.getId(), modifiedBook);

        // check the caches are modified
        assertAll(
                () -> assertThat(result.status()).isEqualTo(CrudResult.Status.UPDATED),
                () -> assertThat(service.findByCategory(categoryName)).hasSize(size),
                () -> assertThat(service.findByCategory(categoryName)).contains(modifiedBook),
                () -> assertThat(service.findByCategory(categoryName)).doesNotContain(book),
                () -> assertThat(service.findByTitleAndAuthor(book.getTitle(), newAuthor))
                        .isEqualTo(modifiedBook)
        );
    }

    @Test
    void updateCategory() {
        Book book = seeder.detachedBookCopy(0);
        String categoryName1 = book.getCategory().getName();

        Category category2 = seeder.aDifferentCategoryFrom(categoryName1);
        String categoryName2 = category2.getName();

        // cache things
        int size1 = service.findByCategory(categoryName1).size();
        int size2 = service.findByCategory(categoryName2).size();
        service.findByTitleAndAuthor(book.getTitle(), book.getAuthor());

        // modify
        Book similarBook = book.toBuilder().category(category2).build();
        CrudResult<Long> result = service.update(book.getId(), similarBook);

        // check the caches are modified
        assertAll(
                () -> assertThat(result.status()).isEqualTo(CrudResult.Status.UPDATED),
                () -> assertThat(service.findByCategory(categoryName1)).hasSize(size1 - 1),
                () -> assertThat(service.findByCategory(categoryName2)).hasSize(size2 + 1),
                () -> assertThat(service.findByTitleAndAuthor(book.getTitle(), book.getAuthor()))
                        .isEqualTo(similarBook)
        );
    }
}
