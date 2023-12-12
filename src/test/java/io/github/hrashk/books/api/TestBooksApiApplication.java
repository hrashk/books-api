package io.github.hrashk.books.api;

import io.github.hrashk.books.api.util.DataSeeder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@TestConfiguration(proxyBeanMethods = false)
@Import(DataSeeder.class)
public class TestBooksApiApplication {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:16-alpine")
                .waitingFor(Wait.forListeningPort());
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>("redis:7-alpine")
                .withExposedPorts(6379)
                .waitingFor(Wait.forListeningPort());
    }

    @Bean
    CommandLineRunner seedSampleData(DataSeeder seeder) {
        return args -> seeder.seed(10);
    }

    public static void main(String[] args) {
        SpringApplication.from(BooksApiApplication::main).with(TestBooksApiApplication.class).run(args);
    }

}
