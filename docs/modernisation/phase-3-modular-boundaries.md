# Phase 3 — Modular monolith boundaries

## Decision

IMDBOpen remains a single deployable Spring Boot application and a single Maven module. Domain boundaries are explicit Java package boundaries, not separate services or deployables. No microservices, CQRS framework, event bus, generic repository abstraction, or module-per-Maven-project is introduced.

## Target package layout

Each business capability owns its delivery, application, domain, and persistence concerns:

- `film/{api,application,domain,infrastructure/persistence}`
- `actor/{api,application,domain,infrastructure/persistence}`
- `director/{api,application,domain,infrastructure/persistence}`
- `casting/{api,application,domain,infrastructure/persistence}`
- `genre/{api,application,domain,infrastructure/persistence}`
- `country/{api,application,domain,infrastructure/persistence}`
- `person/{api,domain,infrastructure/persistence}` for canonical identity shared by actor and director roles
- `ingestion/{application,infrastructure/csv}`
- `shared/{api,configuration,infrastructure}` only for genuinely cross-cutting concerns

All packages will use a single lowercase root namespace: `com.example.imdbopen`. Existing public HTTP routes and DTO shapes are preserved where practical during the structural migration.

## Dependency direction

- API controllers depend on application use cases, not Spring Data repositories.
- Application services coordinate domain behaviour and outbound ports; they do not depend on HTTP controllers or DTO serialization.
- Domain entities and value types do not depend on Spring MVC, repositories, ingestion, or other application modules.
- Persistence adapters implement application-owned ports and map the canonical relational schema.
- Cross-module access is through explicit application contracts or domain identifiers; repositories are not shared across module boundaries.
- Ingestion is an adapter/use case and never runs implicitly during application startup.

## Scope discipline

- Keep one Maven module and one runtime process.
- Keep the current JPA/Flyway persistence model; do not add a generic repository layer over Spring Data.
- Avoid bidirectional relationships across module boundaries unless the persistence model requires them.
- Keep one top-level Java type per source file and make the filename match the type.
- Keep package names and source paths aligned; no uppercase package segments.
- Keep shared code small. A class is not shared merely because more than one module can import it.

## Migration sequence

1. Establish executable architecture rules against the current layers.
2. Move the application to the canonical root package and remove broad package scanning.
3. Move domain types, repositories, application services, API controllers, and DTOs into their owning modules in cohesive slices.
4. Introduce application-owned outbound ports where application code currently depends directly on Spring Data.
5. Move CSV readers into ingestion and remove persistence work from extractors.
6. Tighten module dependency rules and add package/type naming conventions.

Each checkpoint must compile, pass unit and MariaDB integration tests, pass Checkstyle and architecture tests, and produce a green PR CI run before it is considered complete.
