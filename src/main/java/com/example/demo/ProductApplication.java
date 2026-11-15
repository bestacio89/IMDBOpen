package com.example.demo;

import utilities.database.DatabaseInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.demo",
        "entities",
        "utilities",
        "service",
        "web"
})
@EntityScan(basePackages = "entities")
@EnableJpaRepositories(basePackages = "persistence.repository")
public class ProductApplication implements CommandLineRunner {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        databaseInitializer.createDatabase();
    }
}
