# Docker Compose

This directory contains the Docker Compose definition for the IMDBOpen application and its MariaDB database.

## Prerequisites

- Docker Engine
- Docker Compose v2

## Configure credentials

From the repository root, copy the safe template:

```sh
cp Docker/.env.production.example Docker/.env.production
```

Edit `Docker/.env.production` and provide unique values for:

- `MARIADB_ROOT_PASSWORD`
- `MARIADB_USER`
- `MARIADB_PASSWORD`

The database name defaults to `ImdbOpenDB`. The application database account is created by the MariaDB container and is used by the application.

**Do not commit `Docker/.env.production`.** It is ignored by Git. Never reuse credentials that have previously been committed to the repository.

## Build and start

Run from the repository root:

```sh
docker compose --env-file Docker/.env.production --file Docker/docker-compose.yml up --build --detach
```

- Application: http://localhost:8080
- MariaDB: localhost:3306

The application image is built with the repository's Maven Wrapper and Java 21.

## Stop the services

```sh
docker compose --env-file Docker/.env.production --file Docker/docker-compose.yml down
```

The MariaDB data directory is persisted under `Docker/MariaDBData`; stopping the containers does not remove it.

## Production CI

The GitHub Actions workflow uses the `Production` environment. Configure these environment secrets:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `MARIADB_ROOT_PASSWORD`
- `MARIADB_USER`
- `MARIADB_PASSWORD`

Optionally configure the `MARIADB_DATABASE` environment variable; it defaults to `ImdbOpenDB`.

The Docker Hub token must be a newly issued token. Revoke any token previously committed to the repository.
