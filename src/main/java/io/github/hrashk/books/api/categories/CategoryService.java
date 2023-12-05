package io.github.hrashk.books.api.categories;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repository;

    public Category getOrAdd(String name) {
        List<Category> categories = repository.findByName(name);

        if (!categories.isEmpty())
            return categories.get(0);

        var newCategory = new Category().toBuilder().name(name).build();

        return repository.save(newCategory);
    }
}
