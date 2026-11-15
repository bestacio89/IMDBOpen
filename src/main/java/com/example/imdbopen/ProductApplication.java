package com.example.imdbopen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Application entry point. Legacy package scans remain temporarily explicit while
 * production types are migrated into the domain-owned package structure.
 */
@SpringBootApplication(scanBasePackages = {
        "com.example.imdbopen",
        "com.example.demo",
        "entities",
        "utilities",
        "service",
        "web",
        "exceptions"
})
@EntityScan(basePackages = {"entities", "com.example.imdbopen"})
@EnableJpaRepositories(basePackages = {"persistence.repository", "com.example.imdbopen.genre.infrastructure.persistence"})
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
