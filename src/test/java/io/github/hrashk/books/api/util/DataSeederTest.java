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
                () -> assertThat(seeder.authors()).as("Authors").hasSize(size),
                () -> assertThat(seeder.authors()).as("Author ids").noneMatch(a -> a.getId() == null),
                () -> assertThat(seeder.news()).as("News").hasSize(size),
                () -> assertThat(seeder.news()).as("News ids").noneMatch(a -> a.getId() == null),
                () -> assertThat(seeder.categories()).as("Categories").hasSize(size),
                () -> assertThat(seeder.categories()).as("Categor ids").noneMatch(a -> a.getId() == null),
                () -> assertThat(seeder.comments()).as("Comments").hasSize(size),
                () -> assertThat(seeder.comments()).as("Comment ids").noneMatch(a -> a.getId() == null)
        );
    }
}
