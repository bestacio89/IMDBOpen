# Phase 2 — Persistence and database foundations assessment

> **Historical baseline report.** The observations below describe the repository before Phase 2 implementation. The source dump was subsequently audited, the canonical schema was approved, and PR #27 implements Flyway, the aligned JPA model and MariaDB verification. This report is retained to preserve the original findings.

## Implementation outcome

- Flyway is now the single schema authority for clean MariaDB databases.
- Hibernate uses `ddl-auto=validate`; the manual `DatabaseInitializer` and its startup import path have been removed.
- JPA entities now map the canonical `film`, `country`, `genre`, `person`, `actor`, `director`, `film_genre`, `film_director` and `film_cast` structures.
- IMDb `tconst`/`nconst` identifiers resolve to internal foreign keys; actor and director roles share one `person` identity.
- Principal-cast state is stored on the existing `film_cast` row; the separate persistence representation was removed.
- GitHub Actions provisions MariaDB 11.4 as a service. The CI suite verifies migration execution, Hibernate schema validation and relational persistence; this is not a Testcontainers setup.
- CSV ingestion remains a separate subsequent phase and is not run at application startup.


**Assessment baseline:** `develop` at `409498ea71b95c8c3f5b9da05bf9bc3725aa91cc`  
**Assessment branch:** `chore/phase-2-persistence-foundations`  
**Scope:** source-controlled persistence configuration, JPA mappings, schema bootstrap, repository contracts, seed/import paths, Docker database configuration and test configuration.

## Executive finding (at assessment baseline)

The repository does not currently contain one authoritative, reproducible database schema.

The effective schema is being assembled from multiple, conflicting mechanisms:

1. Hibernate schema mutation through `spring.jpa.hibernate.ddl-auto=update`.
2. Hand-written DDL in `DatabaseInitializer`.
3. JPA annotations and implicit naming rules.
4. A dormant Liquibase changelog that creates an unrelated `product` table.
5. Raw SQL writes from CSV extractors into join tables.

The checked-in SQL dump `src/main/resources/db/dump-testimdb.sql` is populated (8,254,307 bytes). A CI audit confirms 11 `CREATE TABLE` statements and 15 `INSERT INTO` statements. It is therefore a potentially valuable schema-and-data baseline, not an empty placeholder. Its DDL, data coverage, export provenance and relationship constraints must be inspected before treating it as authoritative for any live database.

**Baseline decision:** do not activate Liquibase, introduce Flyway, switch Hibernate to `validate`, drop tables, rename columns, or rewrite entity relationships until the checked-in dump has been fully reconciled with the JPA mappings and manual DDL. This gate was completed by the CI dataset audit and the target-model decision documented below. The implementation deliberately targets a clean database; any non-empty deployed database still requires a separate backup-first adoption procedure.

## Current schema authority and startup behaviour

| Mechanism | Current state | Finding |
|---|---|---|
| Hibernate DDL | `ddl-auto=update` in main configuration | Mutates the live schema at application startup; no reviewed migration history |
| Manual initializer | Invoked by `ProductApplication.run()` on every startup | Issues separate DDL and then runs the role CSV import |
| Liquibase | No active dependency; changelog and misspelled `liquidbase.properties` remain | Dormant and unsafe to activate: changelog creates unrelated `product` table and properties specify PostgreSQL |
| Flyway | No dependency or migration set | Documentation references Flyway, but implementation is absent |
| Test schema | H2 in-memory with `ddl-auto=create-drop` | Does not validate MariaDB DDL, types, indexes, constraints or dialect behaviour |

### Startup initializer risks

`DatabaseInitializer.createDatabase()` currently:

- Executes `CREATE SCHEMA IF NOT EXISTS bq6m2mlrafs4wqh3gcgt`, but does not switch the connection to that schema. Subsequent DDL runs against the datasource's configured default database.
- Creates tables whose columns and types conflict with the current JPA mappings (details below).
- Does not use try-with-resources for the statements it creates.
- Catches SQL exceptions, prints the stack trace and allows the application startup path to continue.
- Calls `populateDatabase()` on every application startup. Only the role extractor is currently enabled.
- Uses a filesystem-relative CSV path (`src/main/resources/CSV/roles.csv`). The Docker runtime image copies only the packaged JAR, not the source tree, so this path is not available in the container image.
- Performs schema creation and data import in one component, without a clear migration/seed lifecycle.

The role extractor itself catches row-level failures and continues. Since failures are swallowed inside the transactional method, a partial import can be reported as a completed invocation. It also writes to `film_acteur` through `JdbcTemplate` in addition to persisting `Role`.

## Entity-to-DDL reconciliation

The following compares source-controlled JPA annotations with the explicit DDL in `DatabaseInitializer`. It is not a comparison against the live MariaDB schema.

| Entity/table | JPA mapping | Manual DDL | Mismatch / risk |
|---|---|---|---|
| `Film` / `Film` | `id`, `imdb`, `nom`, `annee`, `rating`, `url`, `lieu_tour`, `langue`, `resume`, `genres`, `id_pays` plus `film_genre` | `id`, `annee`, `langue`, `rating`, `resume`, `url` | Manual DDL omits multiple mapped columns and the country/genre relationships. Table-name case differs (`Film` vs `film`), which is relevant on case-sensitive MySQL/MariaDB deployments. |
| `Personne` / `personne` | `id`, `identite`, string `date_naissance`, `lieu_naissance`, `url` | `id`, `nom`, `prenom`, DATETIME `date_naissance`, `lieu_naissance` | Different identity representation, date type and missing URL column. |
| `Acteur` / `acteur` | `id`, `personne_id`, `id_imdb`, string `taille` | `id_acteur`, `personne_id`, `id_imdb`, DOUBLE `taille` | Primary-key column and height type differ. |
| `Realisateur` / `realisateur` | `id`, `personne_id`, `id_imdb`, `created_date`, `updated_date` | No corresponding table | Entity depends on Hibernate-generated DDL. |
| `Pays` / `Pays` | `id`, `name` | No corresponding table | Entity depends on Hibernate-generated DDL; table-name case needs a consistent policy. |
| `Genre` / `Genre` and `film_genre` | `id`, unique non-null `name`; join table `film_genre(film_id, genre_id)` | No corresponding tables | Relationship schema is not represented by the initializer. |
| `Role` / `role` | `id`, string `film_id`, string `acteur_id`, `role_name` | `id`, `role_name`, BIGINT `film_id`, BIGINT `actor_id` | Both foreign-key-like columns differ in type/meaning; `actor_id` vs `acteur_id` naming mismatch. |
| `CastingPrincipal` / `casting_principal` | `id_casting`, string `film_id`, string `acteur_id` | No corresponding table | Entity depends on Hibernate-generated DDL. |
| Raw association tables | CSV extractors write `film_acteur` and `film_realisateur` | Creates both with IMDb-ID string columns | Not represented as active JPA associations; data integrity is not enforced through mapped foreign keys. |

### Checked-in SQL export — verified historical schema

The CI audit read the full 8,254,307-byte SQL export and extracted 11 table definitions. All are InnoDB tables using `utf8mb4_general_ci`. The tables are:

`acteur`, `casting_principal`, `film`, `film_acteur`, `film_genre`, `film_realisateur`, `genre`, `pays`, `personne`, `realisateur`, `role`.

Important constraints and identifier semantics in that export:

- `acteur.personne_id` and `realisateur.personne_id` are unique foreign keys to `personne.id`, allowing one actor row and one director row per shared person.
- `film.id_pays` references `pays.id`; `film_genre` has a composite `(film_id, genre_id)` primary key and foreign keys to `film` and `genre`.
- `genre.name` is unique. `pays.name` is not unique.
- `film.imdb`, `acteur.id_imdb` and `realisateur.id_imdb` have no unique constraints in this export.
- `personne` does not contain an IMDb identifier. External name IDs are stored separately on `acteur` and `realisateur`.
- `role`, `casting_principal`, `film_acteur` and `film_realisateur` store IMDb identifiers as `VARCHAR` values and have no foreign keys to the entity tables. `film_acteur` and `film_realisateur` are raw association tables, not constrained relational joins.

The 15 `INSERT` statements contain 156,825 rows across the exported tables: `acteur` 27,701; `casting_principal` 4,227; `film` 2,689; `film_acteur` 44,488; `film_realisateur` 2,467; `genre` 28; `pays` 40; `personne` 29,073; `realisateur` 1,624; and `role` 44,488.

Comparison with the CSV audit shows the export contains 18 fewer actor rows than `acteurs.csv` (27,701 vs 27,719) and one fewer director row than the unique director identifiers (1,624 vs 1,625). Film, role and principal-cast row counts match their CSV-derived expectations. `film_realisateur` contains one more row than its CSV (2,467 vs 2,466), consistent with the existing importer inserting the header row. The actor and director source CSVs share 253 IMDb name IDs. In the SQL export, the actor and director role tables contain 29,325 rows while `personne` contains 29,073, implying 252 shared people in the exported database; the one-person difference is consistent with the export's missing actor/director records. The source identifier overlap confirms that `nconst`, not name/date matching, should be the canonical person identity.

This export is materially closer to the current Hibernate/JPA schema than the hand-written DDL in `DatabaseInitializer` (notably, the exported `role.film_id` and `role.acteur_id` are strings, not BIGINTs). It is still a historical schema snapshot, not proof that the currently deployed database has not drifted.

## Identifier semantics and relationship model

The current source mixes two identifier domains:

- Internal database identities: `Long` generated primary keys on Film, Acteur, Personne, Realisateur, Genre and Pays.
- External IMDb identifiers: strings such as `tt0014137` and `nm0000005` in CSV files.

The `Role` and `CastingPrincipal` entities store film/actor identifiers as strings, and the CSV relationship files contain IMDb IDs. These are not the same values as the generated `Film.id` and `Acteur.id` primary keys. The current DDL's BIGINT definitions for role film/actor references therefore do not match the application model.

No FK migration should be designed until the intended relationship identity is agreed and the live data has been profiled. The target design should normally resolve external IMDb identifiers to internal entities at the import boundary, then persist relationships using typed entity associations and database foreign keys. That is a target direction, not a safe in-place migration assumption.

## Import-path findings relevant to persistence

- `FilmExtractor` creates/fetches Genre entities but assigns only the legacy delimited `Film.genres` string. It does not populate `Film.genresl`, so the JPA `film_genre` join table is not populated by this importer.
- `FilmExtractor` truncates long summaries and uses sentinel strings such as `N/A`; the entity also marks several fields as `@NotBlank`. This is a data-quality contract that needs an explicit import policy before tightening database nullability.
- `RoleExtractor` reads three positional fields (film IMDb ID, actor IMDb ID, role name), persists IMDb IDs in string fields and separately inserts them into `film_acteur`.
- `FilmRealisateurExtractor` inserts raw IMDb IDs directly into `film_realisateur`; the corresponding JPA association is commented out. Unlike the other extractors, it does not skip the CSV header, so its first attempted insert uses header labels as identifiers.
- `CastingPrincipalExtractor` persists string IMDb IDs in a separate `casting_principal` entity. Its source file contains film/actor identifier pairs.
- Importers use positional CSV indexing and catch broad exceptions per row. They are not yet suitable as a trusted schema migration or production seeding mechanism.

## Configuration and operational observations

- Main profile points to MariaDB and defaults to `ddl-auto=update`; credentials are environment-driven.
- Docker Compose persists MariaDB under `Docker/MariaDBData` and passes an application-scoped DB user. The Compose service does not mount a database initialization script.
- The MariaDB container is based on MariaDB 10.8.
- The application container is a JRE image containing the packaged JAR only.
- Tests use H2 and disable Liquibase. The context test mocks `DatabaseInitializer`, so it does not exercise the startup DDL or CSV import against MariaDB.
- `db/dump-testimdb.sql` is an 8.25 MB populated export containing 11 table definitions and 15 insert statements. Its exact DDL and data coverage are being audited in CI; it must not be discarded or treated as current-live truth without provenance checks.

## Proposed Phase 2 sequence

### 2.1 — Capture and baseline the real database

First inspect the checked-in 8.25 MB SQL export: extract all table definitions, indexes, constraints, character sets, collations and data coverage. Determine its export provenance and whether it matches the intended deployed database. If the deployed database must be preserved and the dump is stale or incomplete, capture a fresh schema-only and data backup from the live MariaDB instance. Profile MariaDB version, row counts, duplicate IMDb identifiers and orphan relationships.

### 2.2 — Decide the canonical persistence contract

Resolve, explicitly:

- Physical table and column naming/casing.
- Internal primary keys versus external IMDb identifiers.
- Canonical film–genre, film–director, film–actor/role and casting relationships.
- Personne inheritance/composition semantics for actors and directors.
- Field types, lengths, nullability, unique constraints and indexes.
- Whether existing data is to be preserved and how legacy rows are mapped.

### 2.3 — Select and introduce one migration authority

Use a single versioned migration system. Liquibase can be considered because XML changelog assets already exist, but the existing changelog is not valid for this domain and must not be activated as-is. Flyway is also viable, but adding it without the schema baseline would be premature.

Migrations must be tested against MariaDB, not inferred from H2 behaviour.

### 2.4 — Reconcile JPA mappings and startup lifecycle

After the baseline and migration plan are approved:

- Remove manual table DDL from application startup.
- Remove schema mutation and data imports from `CommandLineRunner`.
- Make schema validation the normal application startup policy after migrations.
- Separate any intentional reference-data seeding from schema migration and make it idempotent.
- Remove raw SQL relationship writes in favour of the agreed persistence model.
- Add MariaDB integration coverage for migrations, mappings, constraints and transaction semantics.

## Gate before schema-changing implementation

**Do not implement a migration or alter production table mappings until the checked-in SQL dump has been inspected and reconciled against the source mappings.** The dump contains real schema and data, while the manual DDL and JPA mappings are contradictory. Guessing at its semantics risks orphaning or mis-associating IMDb-linked data.

If the deployed database is disposable and contains no data to preserve, the clean-schema path remains available after the dump has been audited for useful source semantics. If data must be retained, compare the dump against a fresh backup of the live MariaDB instance and design a separate adoption procedure.


## Target model follow-up

The canonical target model is documented in [phase-2-target-data-model.md](phase-2-target-data-model.md). It resolves the source-level identifier ambiguity and records the intended film, person, actor, director, genre, cast-credit and country relationships.

The implementation now provides:

- Flyway SQL migrations as the single schema authority for MariaDB.
- Generated internal `BIGINT` primary keys, with IMDb `tconst`/`nconst` values retained as unique external identifiers.
- Relationship rows use internal foreign keys after ingestion resolves source identifiers.
- A normalised shared `person` record with actor/director role tables.
- One film/actor credit with principal-cast state and zero or more character-role rows.
- Hibernate schema validation after migration, not automatic schema mutation.

This target contract is for a clean database. Existing non-empty database adoption remains a separate, backup-first operation; the application must not guess a baseline or silently mutate an unknown schema. GitHub Actions now provisions MariaDB 11.4 and verifies Flyway migration, JPA schema validation and persistence operations against that real database.
