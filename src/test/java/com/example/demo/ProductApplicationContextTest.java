package com.example.demo;

import com.example.imdbopen.ProductApplication;
import com.example.imdbopen.actor.application.port.out.ActorStore;
import com.example.imdbopen.country.application.port.out.CountryStore;
import com.example.imdbopen.film.domain.Film;
import com.example.imdbopen.person.application.port.out.PersonStore;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import persistence.repository.IFilmRepository;
import web.controller.FilmController;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ProductApplication.class)
@ActiveProfiles("test")
class ProductApplicationContextTest {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private IFilmRepository filmRepository;
    @Autowired
    private CountryStore countryStore;
    @Autowired
    private ActorStore actorStore;
    @Autowired
    private PersonStore personStore;

    @Test
    void discoversFilmEntityRepositoryControllerAndModularStores() {
        assertNotNull(filmRepository);
        assertNotNull(countryStore);
        assertNotNull(actorStore);
        assertNotNull(personStore);
        assertNotNull(applicationContext.getBean(FilmController.class));
        assertTrue(entityManagerFactory.getMetamodel().getEntities().stream()
                .anyMatch(entityType -> entityType.getJavaType().equals(Film.class)));
    }
}
