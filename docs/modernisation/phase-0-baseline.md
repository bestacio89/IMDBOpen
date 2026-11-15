# IMDBOpen — Phase 0 Baseline

**Baseline branch:** `develop`  
**Assessment date:** 2026-11-15  
**Aegis report:** `AegisReport_IMDBOpen_20261115_170310.json`

## 1. Build and test baseline

The GitHub Actions Java 21 verification pipeline has completed successfully with the application-context smoke test enabled:

| Check | Result |
|---|---:|
| Maven Wrapper | Maven 3.9.16 |
| Maven lifecycle | `clean verify` succeeded |
| Tests | 77 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 0 |
| Application context smoke test | Passed |
| Docker Compose configuration validation | Passed |
| Checkstyle | Passed, with an outdated-engine warning |
| Application artifact | Uploaded |
| Verification reports | Uploaded |

The repository previously pinned Maven 3.5.3. The wrapper now pins Maven 3.9.16. The context test verifies discovery of the Film JPA entity, `IFilmRepository` and `FilmController`.

The context test uses H2 and mocks `DatabaseInitializer` to avoid running the existing startup-time DDL and CSV import. It verifies Spring wiring and JPA metadata, but it does not verify MariaDB-specific behaviour or the real startup import path. MariaDB integration tests remain a later gate.

The final workflow definition has also been updated to avoid duplicate push/PR runs, isolate concurrency by Git ref, validate Compose configuration, and skip CI for documentation-only pushes. The current PR-head run remains the merge gate.

## 2. Aegis assessment — interpretation

The supplied Aegis report scanned 105 files and recorded 12 violations.

| Metric | Reported value |
|---|---:|
| Project Health Index | 98.15 |
| Maintainability Index | 98.38 |
| Resilience Index | 0.739 |
| Weighted Compliance | 0.981 |
| Rule Evaluation Coverage | 53.12% |
| Registered Rule Evaluation Coverage | 22.37% |
| Rule Capability Coverage | 72.37% |
| Coverage-adjusted Health Index | 52.14 |
| Assessment complete | No |

The 98.15 Project Health Index must not be interpreted as proof of architectural completeness. The assessment is explicitly incomplete, rule coverage is partial, and the coverage-adjusted index is materially lower.

The report records 76 registered rules, 32 applicable rules, 21 unsupported rules and 17 evaluated rules. It reports 731 passing evaluations, 12 violating evaluations and 55 evaluations with insufficient evidence.

### 2.1 Semantic-analysis limitations

The report contains large groups of JDT errors for unresolved Spring, Jakarta and Swagger types. It also reports that a complete resolved external classpath was unavailable. Four Spring Boot starter dependencies were observed with unresolved `unspecified` versions.

Consequently, diagnostics such as unresolved Spring annotations, SpringApplication, Swagger types and Lombok-generated members must not be treated as independent source defects without reproducing them using the Maven-resolved classpath and annotation processing.

The GitHub Maven build now succeeds. Re-running Aegis with a complete Maven classpath is a separate verification task.

### 2.2 Rule findings requiring source-level interpretation

The 12 reported violations fall into these groups:

- Logging integrity: one finding.
- FilmService facade public-surface rule: one finding; verify whether the rule's Facade interpretation is semantically applicable to an application service before changing the design.
- Complexity: ActorExtractor, FilmExtractor and RealisateurExtractor.
- Maintainability: FilmController, ActorExtractor, FilmExtractor, RealisateurExtractor, CastingPrincipalService and FilmService.
- Documentation coverage: one finding.

Complexity and maintainability results are prioritisation signals, not a substitute for code-level analysis. The source-level findings below drive the actual backlog.

## 3. Defect register

### P0 — Security, bootstrap and persistence

| ID | Finding | Evidence / impact | Status / planned response |
|---|---|---|---|
| P0-SEC-01 | Credentials were committed in application configuration, Docker configuration and the production workflow. | Database and Docker Hub credentials were present in tracked files. Deleting them from the current tree does not remove them from Git history. | Removed from the current tree. Owner must revoke/reissue the Docker Hub token, rotate MariaDB root/application credentials and configure GitHub Production environment secrets. |
| P0-BOOT-01 | Spring package and entity scanning were inconsistent. | The original scan used an uppercase entity package and omitted the `web` package. | Fixed in this PR. The application now scans the declared package roots; the context test verifies Film, IFilmRepository and FilmController discovery. |
| P0-DB-01 | Database schema has competing owners. | DatabaseInitializer performs manual DDL while Hibernate is configured with `ddl-auto=update`; inspected DDL and entity definitions are not fully aligned. | Still open. Inventory the actual schema and data; establish one migration authority; remove manual DDL only after migration coverage exists. |
| P0-DB-02 | No active schema migration framework is configured. | The Liquibase dependency in `pom.xml` is commented out; no Flyway dependency is present. The remaining Liquibase properties file is named `liquidbase.properties`, declares a PostgreSQL driver despite MariaDB being used, and its changelog creates a generic `product` table unrelated to the current domain. | Treat the Liquibase files as stale until proven otherwise. Phase 2 will choose and configure one migration tool after schema/data inventory; do not activate the placeholder changelog or run two migration systems. |

### P1 — Data ingestion and domain integrity

| ID | Finding | Evidence / impact | Planned response |
|---|---|---|---|
| P1-ING-01 | CSV extractors mix parsing, validation, orchestration and persistence. | Extractors combine repository operations and direct SQL, use positional fields and swallow row or infrastructure exceptions. | Introduce a shared ingestion pipeline with typed records, validation, explicit failure policy, idempotency and import summaries. |
| P1-ING-02 | Import transaction semantics can report partial success as success. | Row failures are caught and processing continues inside transactional import methods. | Separate data-quality rejects from infrastructure failures; define batch transaction boundaries and durable import results. |
| P1-DOM-01 | Film genre data has multiple representations. | Film contains both a string genre field and a collection representation. | Inspect stored data and define one canonical relationship model before migration. |
| P1-DOM-02 | Relationship persistence is split between JPA and direct SQL. | Some import paths persist entities through repositories and relationship rows through JdbcTemplate. | Consolidate relationship writes behind the persistence boundary and verify referential integrity. |

### P1 — Query efficiency and API behaviour

| ID | Finding | Evidence / impact | Planned response |
|---|---|---|---|
| P1-QUERY-01 | Film filtering and sorting occur after loading all films. | FilmService starts from `findAll()` and applies stream filtering/sorting in application memory. | Move filtering, sorting and pagination into database queries with typed criteria and bounded result sets. |
| P1-API-01 | Controller and service exception handling is repetitive. | FilmController and service methods contain repeated catch/wrap patterns. | Introduce consistent validation and global API exception handling after preserving current response behaviour. |

### P2 — Technology and maintainability

| ID | Finding | Evidence / impact | Planned response |
|---|---|---|---|
| P2-TECH-01 | Springfox was incompatible with the Jakarta-based application. | The context test failed with `ClassNotFoundException: javax.servlet.http.HttpServletRequest` from Springfox 3.0.0. | Fixed in this PR by replacing Springfox with Springdoc 2.6.0, which is the compatible Springdoc line for Spring Boot 3.3.x. |
| P2-TECH-02 | CSV libraries overlap. | Both OpenCSV and Commons CSV are declared. | Select one library based on actual parser requirements and remove the unused dependency. |
| P2-TEST-01 | Test dependencies mix JUnit 4 and Spring Boot Test. | JUnit 4 is explicitly declared alongside Spring Boot Test. | Inventory tests and standardise on JUnit Jupiter. |
| P2-BUILD-01 | Checkstyle engine is outdated. | Maven verification passes but emits a warning that the Checkstyle version is below the recommended minimum. | Upgrade Checkstyle and its Maven plugin in the framework/tooling phase. |
| P2-LOMBOK-01 | Lombok builder defaults are silently ignored. | Film.genresl and Genre.films have initialisers that Lombok `@Builder` ignores. | Verify intended defaults; use `@Builder.Default` or remove inappropriate builders/initialisers. |
| P2-LOMBOK-02 | Lombok accessors are redundantly declared. | RoleDTO and Role combine Lombok accessor annotations with explicit getter/setter methods. | Keep one accessor-generation strategy per type. |
| P2-DOC-01 | Project documentation is not operationally useful. | Root README is a short note rather than build, configuration, architecture or run documentation. | Replace with reproducible developer setup and architecture documentation in the documentation phase. |

## 4. Phase 0 changes in this PR

- Replaces the legacy test workflow with one Java 21 GitHub Actions pipeline for PRs to `develop`/`main` and pushes to those protected branches.
- Uses the Maven Wrapper and `clean verify`; failures are not suppressed.
- Pins GitHub Actions to verified full commit SHAs using Node.js 24 runtimes.
- Publishes test/quality reports and the application JAR.
- Validates Docker Compose configuration using CI-only placeholder values.
- Adds an application-context smoke test for entity, repository and controller discovery.
- Corrects Spring package/entity scanning.
- Replaces incompatible Springfox with Springdoc 2.6.0.
- Externalises datasource credentials.
- Removes the tracked production environment file and provides a safe example template.
- Removes credentials from the MariaDB Dockerfile and production workflow.
- Requires GitHub Production environment secrets for Docker Hub and MariaDB deployment.
- Aligns the Compose database name with the application's default database name.
- Uses the Maven Wrapper in the application Docker build.
- Excludes environment files and local database data from the Docker build context.
- Records the Aegis assessment interpretation and prioritised defect register.

## 5. Outstanding Phase 0 gates

- [ ] Final GitHub Actions run green on the complete PR head after the latest workflow changes.
- [ ] Revoke the previously committed Docker Hub token and issue a replacement.
- [ ] Rotate the previously committed MariaDB root and application credentials.
- [ ] Configure `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `MARIADB_ROOT_PASSWORD`, `MARIADB_USER` and `MARIADB_PASSWORD` in the GitHub `Production` environment.
- [ ] Confirm branch protection requires the Java CI status check before merge; this setting could not be verified through the available GitHub integration.
- [ ] Re-run Aegis with the resolved Maven classpath and annotation processing, then reconcile remaining diagnostics.
- [ ] Verify the actual MariaDB startup/import path separately; the H2 context test deliberately mocks DatabaseInitializer.

No Spring Boot upgrade, package restructuring, migration implementation or domain rewrite is included in Phase 0.
