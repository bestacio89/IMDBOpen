package com.example.imdbopen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.imdbopen",
        "com.example.demo",
        "exceptions"
})
@EntityScan(basePackages = "com.example.imdbopen")
@EnableJpaRepositories(basePackages = {
        "com.example.imdbopen.genre.infrastructure.persistence",
        "com.example.imdbopen.country.infrastructure.persistence",
        "com.example.imdbopen.actor.infrastructure.persistence",
        "com.example.imdbopen.person.infrastructure.persistence",
        "com.example.imdbopen.director.infrastructure.persistence",
        "com.example.imdbopen.casting.infrastructure.persistence",
        "com.example.imdbopen.film.infrastructure.persistence"
})
public class ProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
