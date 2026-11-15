package com.example.demo;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class FlywaySchemaMigrationTest {

    private static final Set<String> EXPECTED_TABLES = Set.of(
            "country", "film", "genre", "film_genre", "person",
            "actor", "director", "film_director", "film_cast");

    @Container
    private static final MariaDBContainer<?> DATABASE =
            new MariaDBContainer<>("mariadb:11.4")
                    .withDatabaseName("imdbopen")
                    .withUsername("imdbopen")
                    .withPassword("test-password");

    @Test
    void migratesCleanMariaDbToTheCanonicalSchema() throws SQLException {
        Flyway.configure()
                .dataSource(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();

        try (Connection connection = DATABASE.createConnection("");
             Statement statement = connection.createStatement()) {
            DatabaseMetaData metadata = connection.getMetaData();
            Set<String> actualTables;
            try (ResultSet tables = metadata.getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
                actualTables = Stream.generate(() -> readTableName(tables))
                        .takeWhile(name -> name != null)
                        .collect(Collectors.toSet());
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
            assertNotNull(metadata.getPrimaryKeys(connection.getCatalog(), null, "film_cast"));
            assertFalse(metadata.getImportedKeys(connection.getCatalog(), null, "film_cast").isClosed());
        }
    }

    private static String readTableName(ResultSet tables) {
        try {
            return tables.next() ? tables.getString("TABLE_NAME") : null;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to read migrated table metadata", exception);
        }
    }
}
