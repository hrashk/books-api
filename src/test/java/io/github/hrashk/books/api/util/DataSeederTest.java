package io.github.hrashk.books.api.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class DataSeederTest extends ServiceTest {
    @Test
    void sampleDataIsLoaded() {
        int size = 10;
        seeder.flush();

        assertAll(
                () -> assertThat(seeder.books()).as("News").hasSize(size),
                () -> assertThat(seeder.books()).as("News ids").noneMatch(a -> a.getId() == null),
                () -> assertThat(seeder.categories()).as("Categories").hasSize(size),
                () -> assertThat(seeder.categories()).as("Categor ids").noneMatch(a -> a.getId() == null)
        );
    }
}
