package io.github.hrashk.books.api.books;

import io.github.hrashk.books.api.categories.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books",
        uniqueConstraints = @UniqueConstraint(name = "UniqueTitleAndAuthor", columnNames = {"title", "author"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Book {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Category category;
}
