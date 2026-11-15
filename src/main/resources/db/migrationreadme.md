# MariaDB schema migrations

IMDBOpen uses **Flyway SQL migrations** as the only schema-management authority.

## Migration location

Place versioned SQL migrations in:

```text
src/main/resources/db/migration/
```

Use Flyway's versioned naming convention, for example:

```text
V1__create_initial_schema.sql
V2__add_film_external_rating.sql
```

A migration is immutable after it has been applied to a shared environment. Add a new versioned migration for subsequent schema changes; do not edit an already-applied migration.

## Runtime behaviour

- Flyway applies pending migrations during application startup.
- Hibernate is configured with `spring.jpa.hibernate.ddl-auto=validate`.
- Hibernate validates the migrated schema and must not create, update or drop production tables.
- Liquibase is not active and must not be reintroduced as a second migration authority.
- CSV imports and reference-data ingestion are not schema migrations and do not run during application startup.

## Verification

The GitHub Actions test workflow provisions MariaDB 11.4 as a service and exposes the CI-only datasource through:

- `IMDBOPEN_TEST_DB_URL`
- `IMDBOPEN_TEST_DB_USERNAME`
- `IMDBOPEN_TEST_DB_PASSWORD`

The test suite verifies migration execution, uniqueness and foreign-key enforcement, then starts the Spring application with Flyway enabled and Hibernate validation enabled against the migrated MariaDB schema. It also persists relational associations through their internal foreign keys.

The workflow uses a managed MariaDB service rather than Testcontainers because the test process cannot access a Docker daemon in the GitHub Actions runner environment.

## Existing database adoption

The initial migration targets a clean database. Do not enable `baseline-on-migrate` or mark an existing database as managed without a separately reviewed adoption plan. Any retained database requires a verified backup, schema/data profiling, explicit legacy-to-canonical mapping and rehearsal against a restored copy.
