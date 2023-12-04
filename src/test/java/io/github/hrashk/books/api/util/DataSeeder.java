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
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.LongStream;

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

    public Iterable<Category> sampleCategories(int count) {
        return generateSample(count, this::aRandomCategory);
    }

    public Iterable<Book> sampleBooks(int count) {
        return generateSample(count, this::aRandomBook);
    }

    private <T> List<T> generateSample(int count, Supplier<T> entityGenerator) {
        return LongStream.range(1, count + 1)
                .mapToObj(id -> entityGenerator.get())
                .toList();
    }

    public Category aRandomCategory() {
        return new Category().toBuilder()
                .name(faker.book().genre())
                .build();
    }

    public Book aRandomBook() {
        Book book = new Book().toBuilder()
                .title(faker.book().title())
                .author(faker.book().author())
                .build();

        return book;
    }

    private <T> T randomItem(List<T> items) {
        return items.get(random.nextInt(items.size()));
    }
}
