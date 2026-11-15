package com.example.demo;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

    @Test
    void startsAgainstTheMigratedCanonicalMariaDbSchema() {
        assertNotNull(entityManagerFactory);
        assertNotNull(entityManagerFactory.getMetamodel().entity(
                entities.business.Film.Film.class));
        assertNotNull(entityManagerFactory.getMetamodel().entity(
                entities.business.personne.Personne.class));
        assertNotNull(entityManagerFactory.getMetamodel().entity(
                entities.business.role.Role.class));
    }
}
