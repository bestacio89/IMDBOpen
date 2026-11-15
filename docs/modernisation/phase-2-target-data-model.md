# Phase 2 — Target persistence model

**Status:** proposed target contract for the modernisation  
**Baseline:** `develop` at `409498ea71b95c8c3f5b9da05bf9bc3725aa91cc`

## Design objectives

- Use one authoritative, versioned schema migration mechanism.
- Keep relational integrity in MariaDB with explicit primary keys, foreign keys, uniqueness constraints and indexes.
- Treat IMDb identifiers as external identifiers, not as substitutes for internal relational keys.
- Resolve external identifiers at the ingestion boundary; persist associations through internal foreign keys.
- Keep the model normalised without introducing generic repositories, microservices or speculative abstractions.
- Make schema creation deterministic on a clean database and make existing-database adoption an explicit operational procedure.

## Identifier policy

Every aggregate/entity row uses a generated `BIGINT` primary key. IMDb identifiers remain first-class external identifiers:

- Film: `tconst`, unique and non-null.
- Person: `nconst`, unique and non-null.

All internal relationships reference generated primary keys. CSV import code must resolve `tconst` and `nconst` to persisted entities before writing relationships. No relationship table stores an IMDb identifier as a foreign key.

This preserves stable database joins while retaining the source-system identifiers needed for idempotent imports and API lookups.

## Canonical tables

| Table | Responsibility | Key constraints |
|---|---|---|
| `country` | Country/reference data used by films | Unique normalized name; optional ISO-3166 code |
| `film` | Film metadata | Unique `tconst`; nullable source fields represented as nullable columns |
| `genre` | Controlled genre vocabulary | Unique normalized name |
| `film_genre` | Film-to-genre association | Composite primary key; both columns are foreign keys |
| `person` | Shared person identity and biographical data | Unique `nconst` |
| `actor` | Actor-specific attributes | `person_id` is both PK and FK to `person` |
| `director` | Director role marker | `person_id` is both PK and FK to `person` |
| `film_director` | Film-to-director association | Composite primary key; both columns are foreign keys |
| `film_cast` | Actor participation and character credit in a film | Generated PK; unique film/actor pair; character name and principal-cast flag |

### Film attributes

- `tconst`: external IMDb title identifier.
- `title`: source title.
- `release_year`: nullable integer year, not a free-form string.
- `rating`: nullable decimal with one fractional digit.
- `source_url`: nullable URL/path.
- `filming_locations`: text, because source values can contain multiple locations.
- `language`: nullable source language.
- `synopsis`: text/long text.
- `country_id`: nullable FK to `country`.

The source CSV currently labels the country column `PAYS`; its exact business meaning should remain documented as source-country metadata until verified. It must not be repurposed as a person's nationality.

### Person attributes

- `nconst`: external IMDb name identifier.
- `name`: source identity/name.
- `birth_date`: SQL DATE, nullable. Import must parse source date formats explicitly and report invalid values rather than silently storing arbitrary strings.
- `birth_place`: nullable text.
- `profile_url`: nullable URL/path.

Actor and director are independent role tables referencing the same person identity. A person can therefore be both an actor and a director without duplicated biographical rows. Actor height remains a nullable source value until its unit and format are established; it must not be silently interpreted as centimetres.

### Cast and role semantics

The source files represent two attributes of the same film/actor credit:

- `roles.csv` contains 44,488 unique film/actor pairs, with exactly one role row per pair in the current dataset.
- `castingPrincipal.csv` contains 4,227 unique film/actor pairs, all of which are present in `roles.csv`; the remaining 40,261 role credits are not principal-cast entries.

The target model therefore uses one `film_cast` row per film/actor pair, with `character_name` and `is_principal` attributes. Importing principal cast sets the flag on the same credit rather than creating a second relationship. A separate `cast_role` table is not justified by the current source cardinality and would add unnecessary complexity. Revisit that design only if a future source establishes multiple character records for one actor/film pair. This removes the duplicated `film_acteur` and `casting_principal` relationship representations.

### Director semantics

`film_realisateurs.csv` maps film IMDb identifiers to director IMDb identifiers. The target `film_director` relation stores internal foreign keys after both sides are resolved.

## Mapping decisions

1. Use lowercase snake_case table and column names consistently. Do not rely on case-insensitive table lookup.
2. Use InnoDB and utf8mb4 with an explicitly selected collation.
3. Use explicit foreign-key delete behaviour. Reference rows should not be cascade-deleted from a film/person delete; join/credit rows may cascade from their owning film/person according to the final lifecycle policy.
4. Index every FK and the principal lookup paths: film `tconst`, film title/year, person `nconst` and name, genre name, and cast/director association keys.
5. Do not add soft-delete columns, audit columns, enums, or speculative fields without a demonstrated application requirement.
6. Avoid JPA `CascadeType.ALL` across shared reference data such as genres and countries.
7. JPA associations should be lazy by default; query use cases should load the required graph explicitly.

## Migration authority

Use Flyway with SQL migrations for the MariaDB schema. The schema is relational and the desired DDL should be reviewable as executable SQL. Use the MariaDB/MySQL Flyway database-support module alongside Flyway Core; rely on the Spring Boot dependency-management versions rather than pinning independent versions.

The dormant Liquibase assets are not a second migration authority. They must be removed or archived as part of the migration-framework implementation, not activated.

## Existing database adoption and safety

A clean database is the reference target for the new schema. No migration should infer or rename legacy columns from the current hand-written DDL or JPA annotations.

For any existing non-empty database that must be retained:

1. Take and verify a schema-only and data backup.
2. Profile current rows, duplicate IMDb identifiers, orphan relationship rows, and source-ID mappings.
3. Define and test a separate adoption/baseline procedure against a restored copy.
4. Only then mark the existing schema as managed by Flyway.

A fresh-database migration and an existing-database adoption are different operations. The latter must never be hidden behind `baseline-on-migrate` or automatic table mutation.

## Source evidence and known gaps

The six checked-in datasets were parsed in CI with OpenCSV and audited for record counts, field widths, encodings, empty values, duplicate source identifiers and cross-file references:

| Dataset | Header | Data rows | Encoding | Observed contract |
|---|---|---:|---|---|
| `films.csv` | `ID IMDB; NOM; Column3; RATING; URL; LIEU TOURNAGE; GENRES; LANGUE; RESUME; PAYS` | 2,748 | UTF-8 | `Column3` contains release year values; all rows have 10 fields |
| `acteurs.csv` | `ID IMDB; IDENTITE; DATE NAISSANCE; LIEU NAISSANCE; TAILLE; URL` | 27,719 | Windows-1252 | Height values include units (for example `1.70 m`); dates are English-formatted |
| `roles.csv` | `FILM; ID ACTEUR; PERSONNAGE` | 44,488 | UTF-8 | Film IMDb ID, actor IMDb ID, character name |
| `realisateurs.csv` | `ID; IDENTITE; DATE NAISSANCE; LIEU NAISSANCE; URL` | 1,636 | Windows-1252 | Person IMDb ID and biographical fields; dates are English-formatted |
| `castingPrincipal.csv` | `FILM; ID ACTEUR` | 4,227 | UTF-8 | Principal film/actor pairs |
| `film_realisateurs.csv` | `FILM; ID REALISATEUR; Column3` | 2,466 | UTF-8 | Third field is empty in every row; first two fields form 2,461 unique pairs, with four pairs repeated (maximum three rows) |

The audited association files have zero missing film/person references. Actor IMDb identifiers are unique. The actor and director source files share 253 IMDb name identifiers, confirming that the external `nconst` is the correct cross-role person identity. The film dataset contains 59 duplicate rows across 38 IMDb identifiers (2,689 unique IDs); all 38 duplicated identifiers have conflicting row content. The director dataset contains 11 duplicate rows across 11 IMDb identifiers (1,625 unique IDs); all 11 are conflicting. These conflicts must be resolved through an explicit source precedence or reject policy before uniqueness constraints and import behaviour are finalised.

The role dataset contains exactly one row per film/actor pair (44,488 unique pairs); all 4,227 principal-cast pairs are a subset of those role credits. No pair has multiple role rows. The film/director dataset has 2,466 rows but only 2,461 unique pairs; four pairs repeat, with one appearing three times. The target therefore uses one `film_cast` row with character and principal attributes, and a unique film/director pair; it does not need a separate character-role table.

The UTF-8 film and role files also contain 938 and 575 literal Unicode replacement characters (`U+FFFD`) respectively. This is already-lossy source text, not a decoding problem that can be repaired by changing the reader charset. Recover original values from an authoritative upstream source where possible; otherwise preserve and report the affected values rather than guessing replacements.

The checked-in SQL export `src/main/resources/db/dump-testimdb.sql` is 8,254,307 bytes and contains 11 table definitions and 15 insert statements. CI extracted all DDL and counted 156,825 inserted rows: 27,701 actors, 1,624 directors, 29,073 people, 2,689 films, 44,488 roles, 4,227 principal-cast links and 2,467 film/director links. Compared with the CSV sources, 18 actor records and one unique director record are absent from the export; the film/director relation has one extra row, consistent with the importer not skipping its header. The export is therefore useful historical evidence, but not a complete or unquestioned import baseline.

The film/director importer currently does not skip its header row, unlike the other extractors; the ingestion rewrite must correct this and include a regression test. The current MCD README is conceptual and differs from both current entities and source CSVs. It is not treated as an executable schema contract.

Before ingestion is rewritten, duplicate records must be classified, the historical SQL DDL must be reviewed, and source fields such as country semantics, role cardinality and date parsing must be confirmed.

## Verified field typing and preservation rules

The second CI profile pass established the following field semantics:

- Film years have 175 distinct source values; 102 are not a single integer and appear as year ranges such as `1952�1953`. Preserve the original `release_year_source` and derive nullable `release_year_start` / `release_year_end` values. Do not coerce ranges to one integer or discard the source value.
- Film ratings use both decimal points and decimal commas. Normalising the comma produces numeric values from 2.1 to 9.5; 46 values are `NA`. Store a nullable `DECIMAL(3,1)`, treating `NA` as null.
- Actor heights are expressed in metres. 14,490 values are `NA`, nine are malformed or corrupted, and the remaining values parse numerically (observed range 0.48–2.93 m). Store nullable `DECIMAL(4,2)` metres and retain the source text for malformed values.
- Birth dates contain missing sentinels, full English and French dates, partial dates without a year, and year-only values. The profile currently identifies 8,710 missing and 1,398 non-normalisable actor dates, plus 386 missing and 89 non-normalisable director dates. Store a nullable SQL `DATE` where parsing is unambiguous and preserve the original text in `birth_date_source`; do not invent missing day/month/year components.
- Fourteen film language values are the literal sentinel `None`; normalise those to SQL NULL. There are 40 distinct countries and 28 distinct genre tokens.
- Film synopsis and character text already contain U+FFFD replacement characters. Preserve these as received and report them; recovery requires authoritative source data.

Accordingly, the target schema should use:
- `person.birth_date DATE NULL` plus `birth_date_source VARCHAR(64) NULL`;
- `actor.height_m DECIMAL(4,2) NULL` plus `height_source VARCHAR(32) NULL` for malformed values;
- `film.release_year_source VARCHAR(16) NOT NULL`, `release_year_start SMALLINT NULL`, `release_year_end SMALLINT NULL`;
- `film.rating DECIMAL(3,1) NULL`, `film.language VARCHAR(100) NULL`, and a nullable synopsis;
- explicit `country` and `genre` tables with unique names, plus the `film_genre` association.

## Implementation sequence

1. Introduce Flyway and the initial MariaDB SQL migration for a clean database.
2. Align JPA entities with this contract and set Hibernate to `validate`.
3. Remove `DatabaseInitializer` DDL and startup CSV import; migrations own schema only.
4. Add MariaDB Testcontainers coverage for migration execution, mappings, FK constraints and uniqueness.
5. Rework CSV ingestion separately, resolving external IMDb identifiers to internal entities and reporting rejected rows.
