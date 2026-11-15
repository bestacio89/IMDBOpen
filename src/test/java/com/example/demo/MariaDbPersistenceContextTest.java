package com.example.demo;

import com.example.imdbopen.ProductApplication;
import com.example.imdbopen.actor.domain.Acteur;
import com.example.imdbopen.director.domain.Realisateur;
import com.example.imdbopen.film.domain.Film;
import com.example.imdbopen.film.domain.FilmDirector;
import com.example.imdbopen.film.domain.FilmDirectorId;
import com.example.imdbopen.genre.domain.Genre;
import com.example.imdbopen.person.domain.Personne;
import com.example.imdbopen.casting.domain.Role;
import com.example.imdbopen.country.domain.Pays;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ProductApplication.class, properties = {
        "spring.datasource.url=${IMDBOPEN_TEST_DB_URL}",
        "spring.datasource.username=${IMDBOPEN_TEST_DB_USERNAME}",
        "spring.datasource.password=${IMDBOPEN_TEST_DB_PASSWORD}",
        "spring.datasource.driver-class-name=org.mariadb.jdbc.Driver",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "spring.liquibase.enabled=false"
})
@ContextConfiguration(initializers = MariaDbSchemaInitializer.class)
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
        assertNotNull(entityManagerFactory.getMetamodel().entity(Acteur.class));
        assertNotNull(entityManagerFactory.getMetamodel().entity(Realisateur.class));
        assertNotNull(entityManagerFactory.getMetamodel().entity(Role.class));
        assertNotNull(entityManagerFactory.getMetamodel().entity(Pays.class));
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
    void persistsFilmCountryAssociationThroughInternalForeignKey() {
        Pays country = new Pays(null, "Architecture Test Country");
        entityManager.persist(country);
        Film film = new Film();
        film.setImdb("tt9000003");
        film.setNom("Country Mapping Verification");
        film.setAnnee("2026");
        film.setPays(country);
        entityManager.persist(film);
        entityManager.flush();
        entityManager.clear();
        Film persisted = entityManager.find(Film.class, film.getId());
        assertNotNull(persisted);
        assertEquals(country.getId(), persisted.getPays().getId());
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
