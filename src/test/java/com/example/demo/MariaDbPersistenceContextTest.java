package com.example.demo;

import entities.business.Film.Film;
import entities.business.Film.FilmDirector;
import entities.business.Film.FilmDirectorId;
import entities.business.personne.Personne;
import entities.business.personne.Realisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertNotNull(entityManagerFactory.getMetamodel().entity(entities.business.role.Role.class));
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
}
