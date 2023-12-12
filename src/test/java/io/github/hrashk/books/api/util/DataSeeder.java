package io.github.hrashk.books.api.util;

import io.github.hrashk.books.api.books.Book;
import io.github.hrashk.books.api.books.BookRepository;
import io.github.hrashk.books.api.categories.Category;
import io.github.hrashk.books.api.categories.CategoryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.datafaker.Faker;
import org.springframework.boot.test.context.TestComponent;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@TestComponent
@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class DataSeeder {
    private final BookRepository booksRepo;
    private final CategoryRepository categoryRepo;

    private final Random random = ThreadLocalRandom.current();
    private final Faker faker = new Faker(random);

    private List<Category> categories;
    private List<Book> books;

    public void seed(int count) {
        categories = categoryRepo.saveAll(sampleCategories(count));
        books = booksRepo.saveAll(sampleBooks(count));
    }

    public void flush() {
        categoryRepo.flush();
        booksRepo.flush();
    }

    public void clear() {
        booksRepo.deleteAll();
        categoryRepo.deleteAll();
    }

    public Iterable<Category> sampleCategories(int count) {
        return generateSample(count, this::aRandomCategory, Category::getName);
    }

    public Iterable<Book> sampleBooks(int count) {
        return generateSample(count, this::aRandomBook, b -> b.getTitle() + "|" + b.getAuthor());
    }

    private <T> List<T> generateSample(int count, Supplier<T> entityGenerator, Function<? super T, ?> keyExtractor) {
        return Stream.generate(entityGenerator)
                .filter(distinctByKey(keyExtractor))
                .limit(count).toList();
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public Category aRandomCategory() {
        return new Category().toBuilder()
                .name(faker.book().genre())
                .build();
    }

    public Book aRandomBook() {
        return new Book().toBuilder()
                .title(faker.book().title())
                .author(faker.book().author())
                .category(randomItem(categories))
                .build();
    }

    private <T> T randomItem(List<T> items) {
        return items.get(random.nextInt(items.size()));
    }

    public Category aDifferentCategoryFrom(String category) {
        return categories.stream()
                .filter(c -> !Objects.equals(c.getName(), category))
                .findAny().get();
    }

    /**
     * Detached copies are not modified when the original is saved by the repository.
     */
    public Book detachedBookCopy(int index) {
        return books.get(index).toBuilder().build();
    }
}
