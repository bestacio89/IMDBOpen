package com.example.demo;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywaySchemaMigrationTest {

    private static final Set<String> EXPECTED_TABLES = Set.of(
            "country", "film", "genre", "film_genre", "person",
            "actor", "director", "film_director", "film_cast");

    @Test
    @EnabledIfEnvironmentVariable(named = "IMDBOPEN_TEST_DB_URL", matches = "jdbc:mariadb:.*")
    void migratesCleanMariaDbToTheCanonicalSchema() throws SQLException {
        String url = System.getenv("IMDBOPEN_TEST_DB_URL");
        String username = System.getenv("IMDBOPEN_TEST_DB_USERNAME");
        String password = System.getenv("IMDBOPEN_TEST_DB_PASSWORD");

        Flyway flyway = Flyway.configure()
                .dataSource(url, username, password)
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load();
        flyway.clean();
        flyway.migrate();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            Set<String> actualTables;
            try (ResultSet tables = connection.getMetaData().getTables(
                    connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
                java.util.HashSet<String> names = new java.util.HashSet<>();
                while (tables.next()) {
                    names.add(tables.getString("TABLE_NAME"));
                }
                actualTables = Set.copyOf(names);
            }

            assertTrue(actualTables.containsAll(EXPECTED_TABLES));
            assertEquals(9, actualTables.stream().filter(EXPECTED_TABLES::contains).count());

            statement.executeUpdate("INSERT INTO country (name) VALUES ('United States')");
            statement.executeUpdate("""
                    INSERT INTO film (tconst, title, release_year_source, release_year_start,
                                      release_year_end, rating, country_id)
                    VALUES ('tt0000001', 'Example Film', '1952-1953', 1952, 1953, 7.5, 1)
                    """);
            statement.executeUpdate("INSERT INTO person (nconst, name) VALUES ('nm0000001', 'Example Person')");
            statement.executeUpdate("INSERT INTO actor (person_id, height_m) VALUES (1, 1.70)");
            statement.executeUpdate("INSERT INTO film_cast (film_id, actor_id, character_name, is_principal) "
                    + "VALUES (1, 1, 'Example Character', TRUE)");

            assertThrows(SQLException.class, () -> statement.executeUpdate(
                    "INSERT INTO film (tconst, title, release_year_source) "
                            + "VALUES ('tt0000001', 'Duplicate Film', '1952')"));
            assertThrows(SQLException.class, () -> statement.executeUpdate(
                    "INSERT INTO film_cast (film_id, actor_id) VALUES (1, 999)"));
        }
    }
}
