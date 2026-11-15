package com.example.demo;

import entities.business.Film.Film;
import entities.business.Film.FilmDirector;
import entities.business.Film.FilmDirectorId;
import entities.business.genre.Genre;
import entities.business.personne.Acteur;
import entities.business.personne.Personne;
import entities.business.personne.Realisateur;
import entities.business.role.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=${IMDBOPEN_TEST_DB_URL}",
        "spring.datasource.username=${IMDBOPEN_TEST_DB_USERNAME}",
        "spring.datasource.password=${IMDBOPEN_TEST_DB_PASSWORD}",
        "spring.datasource.driver-class-name=org.mariadb.jdbc.Driver",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "spring.liquibase.enabled=false"
})
@EnabledIfEnvironmentVariable(named = "IMDBOPEN_TEST_DB_URL", matches = "jdbc:mariadb:.*")
class MariaDbPersistenceContextTest {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private EntityManager entityManager;

    @Test
    void startsAgainstTheMigratedCanonicalMariaDbSchema() {
        assertNotNull(entityManagerFactory);
        assertNotNull(entityManagerFactory.getMetamodel().entity(Film.class));
        assertNotNull(entityManagerFactory.getMetamodel().entity(Personne.class));
        assertNotNull(entityManagerFactory.getMetamodel().entity(Role.class));
        assertNotNull(entityManagerFactory.getMetamodel().entity(FilmDirector.class));
    }

    @Test
    @Transactional
    void persistsFilmDirectorAssociationThroughInternalForeignKeys() {
        Film film = new Film();
        film.setImdb("tt9000001");
        film.setNom("Persistence Mapping Verification");
        film.setAnnee("2024");
        entityManager.persist(film);

        Personne person = new Personne("Director Mapping Test", "1990-01-02", null, null);
        person.setNconst("nm9000001");
        entityManager.persist(person);

        Realisateur director = new Realisateur();
        director.setPersonne(person);
        entityManager.persist(director);
        entityManager.flush();

        FilmDirector association = new FilmDirector(film, director);
        entityManager.persist(association);
        entityManager.flush();

        FilmDirector persisted = entityManager.find(FilmDirector.class,
                new FilmDirectorId(film.getId(), director.getId()));
        assertNotNull(persisted);
    }

    @Test
    @Transactional
    void persistsFilmGenreAndActorCastUsingInternalForeignKeys() {
        Film film = new Film();
        film.setImdb("tt9000002");
        film.setNom("Actor Mapping Verification");
        film.setAnnee("2025");
        entityManager.persist(film);

        Genre genre = new Genre();
        genre.setName("Integration Test Genre");
        entityManager.persist(genre);

        Personne person = new Personne("Actor Mapping Test", "1990-01-02", null, null);
        person.setNconst("nm9000002");
        entityManager.persist(person);

        Acteur actor = new Acteur();
        actor.setPersonne(person);
        actor.setTaille("1.70 m");
        entityManager.persist(actor);
        film.setGenresl(new HashSet<>(Set.of(genre)));
        entityManager.flush();

        Role credit = new Role("Integration Test Character", film.getImdb(), actor.getIdImdb());
        credit.setFilm(film);
        credit.setActeur(actor);
        credit.setPrincipal(true);
        entityManager.persist(credit);
        entityManager.flush();

        Role persisted = entityManager.find(Role.class, credit.getId());
        assertNotNull(persisted);
        assertTrue(persisted.isPrincipal());
        assertEquals(1, film.getGenresl().size());
        assertEquals(person.getId(), actor.getId());
    }
}
