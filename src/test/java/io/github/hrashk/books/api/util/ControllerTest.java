package io.github.hrashk.books.api.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = PostgreSQLInitializer.class)
@Import(DataSeeder.class)
public abstract class ControllerTest {
    protected static final Long INVALID_ID = 111222333L;
    @Autowired
    protected TestRestTemplate rest;
    @Autowired
    protected DataSeeder seeder;

    @BeforeEach
    void injectSampleData() {
        seeder.seed(10);
    }

    @AfterEach
    void deleteAllEntities() {
        seeder.clear();
    }

    public <T> ResponseEntity<T> put(String url, Object request, Class<T> responseType, Object... urlVariables) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return rest.exchange(url, HttpMethod.PUT, new HttpEntity<>(request, headers), responseType, urlVariables);
    }

    public <T> ResponseEntity<T> delete(String url, Class<T> responseType, Object... urlVariables) {
        return rest.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, responseType, urlVariables);
    }

    public ResponseEntity<Void> delete(String url, Object... urlVariables) {
        return delete(url, Void.class, urlVariables);
    }
}
