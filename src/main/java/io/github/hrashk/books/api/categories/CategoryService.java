package io.github.hrashk.books.api.categories;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repository;

    public Category getOrAdd(String name) {
        Optional<Category> category = repository.findByName(name);

        if (category.isPresent())
            return category.get();

        var newCategory = new Category().toBuilder().name(name).build();

        return repository.save(newCategory);
    }
}
