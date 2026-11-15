package com.example.demo;

import entities.business.Film.Film;
import persistence.repository.IFilmRepository;
import utilities.database.DatabaseInitializer;
import web.controller.FilmController;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ProductApplicationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private IFilmRepository filmRepository;

    @MockBean
    private DatabaseInitializer databaseInitializer;

    @Test
    void shouldDiscoverFilmEntityRepositoryAndController() {
        assertNotNull(filmRepository);
        assertNotNull(applicationContext.getBean(FilmController.class));
        assertTrue(entityManagerFactory.getMetamodel().getEntities().stream()
                .anyMatch(entityType -> entityType.getJavaType().equals(Film.class)));
    }
}
