# IMDBOpen — Phase 0 Baseline

**Baseline branch:** `develop`  
**Assessment date:** 2026-11-15  
**Aegis report:** `AegisReport_IMDBOpen_20261115_170310.json`

## 1. Build and test baseline

The initial GitHub Actions verification completed successfully on Java 21:

| Check | Result |
|---|---:|
| Maven Wrapper | Maven 3.9.16 |
| Maven lifecycle | `clean verify` succeeded |
| Tests | 76 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 0 |
| Checkstyle | Passed |
| Application artifact | Uploaded |
| Verification reports | Uploaded |

The repository previously pinned Maven 3.5.3. The wrapper now pins Maven 3.9.16. The final CI configuration must be rerun after the Phase 0 hardening changes; the initial green run is a baseline, not proof of the latest revision.

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

### P0 — Security and application bootstrap

| ID | Finding | Evidence / impact | Planned response |
|---|---|---|---|
| P0-SEC-01 | Credentials were committed in application configuration, Docker configuration and the production workflow. | Database and Docker Hub credentials were present in tracked files. Deleting them from the current tree does not remove them from Git history. | Revoke/reissue the Docker Hub token; rotate MariaDB root and application credentials; configure GitHub Production environment secrets. |
| P0-BOOT-01 | Explicit Spring scanning configuration is inconsistent with package declarations. | The application entry point is under `com.example.demo`; explicit scans use a restricted set of packages, omit the web package, and use an uppercase entity package that does not match lowercase declared packages. | Verify with a Spring context and repository/entity discovery test, then simplify scanning around a single root package. |
| P0-DB-01 | Database schema has competing owners. | DatabaseInitializer performs manual DDL while Hibernate is configured with `ddl-auto=update`; inspected DDL and entity definitions are not fully aligned. | Inventory the actual schema and data; introduce one migration authority; remove manual DDL only after migration coverage exists. |

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
| P2-TECH-01 | Spring Boot baseline is old and Springfox is present. | Maven declares Spring Boot 3.3.4 and Springfox 3.0.0. | Review compatibility and upgrade framework/documentation tooling in an isolated phase. |
| P2-TECH-02 | CSV libraries overlap. | Both OpenCSV and Commons CSV are declared. | Select one library based on actual parser requirements and remove the unused dependency. |
| P2-TEST-01 | Test dependencies mix JUnit 4 and Spring Boot Test. | JUnit 4 is explicitly declared alongside Spring Boot Test. | Inventory tests and standardise on JUnit Jupiter. |
| P2-DOC-01 | Project documentation is not operationally useful. | Root README is a short note rather than build, configuration, architecture or run documentation. | Replace with reproducible developer setup and architecture documentation in the documentation phase. |

## 4. Phase 0 changes in this PR

- Introduces one GitHub Actions Java 21 pipeline for branch pushes and PRs to `develop` and `main`.
- Uses the Maven Wrapper and `clean verify`; failures are not suppressed.
- Pins GitHub Actions to verified full commit SHAs using Node.js 24 runtimes.
- Publishes test/quality reports and the application JAR.
- Externalises datasource credentials.
- Removes the tracked production environment file and provides a safe example template.
- Removes credentials from the MariaDB Dockerfile and production workflow.
- Requires GitHub Production environment secrets for Docker Hub and MariaDB deployment.
- Aligns the Compose database name with the application's default database name.
- Uses the Maven Wrapper in the application Docker build.
- Excludes environment files and local database data from the Docker build context.

## 5. Outstanding Phase 0 gates

- [ ] Final GitHub Actions run green on the complete PR head.
- [ ] Revoke the previously committed Docker Hub token and issue a replacement.
- [ ] Rotate the previously committed MariaDB root and application credentials.
- [ ] Configure `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `MARIADB_ROOT_PASSWORD`, `MARIADB_USER` and `MARIADB_PASSWORD` in the GitHub `Production` environment.
- [ ] Confirm branch protection requires the Java CI status check before merge; this setting could not be verified through the available GitHub integration.
- [ ] Re-run Aegis with the resolved Maven classpath and annotation processing, then reconcile remaining diagnostics.

No Spring Boot upgrade, package restructuring or domain rewrite is included in Phase 0.
