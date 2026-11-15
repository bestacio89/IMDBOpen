CREATE TABLE country (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    iso_3166_code CHAR(2) NULL,
    CONSTRAINT pk_country PRIMARY KEY (id),
    CONSTRAINT uk_country_name UNIQUE (name),
    CONSTRAINT uk_country_iso_code UNIQUE (iso_3166_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE film (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tconst VARCHAR(20) NOT NULL,
    title VARCHAR(512) NOT NULL,
    release_year_source VARCHAR(32) NOT NULL,
    release_year_start SMALLINT NULL,
    release_year_end SMALLINT NULL,
    rating DECIMAL(3,1) NULL,
    source_url VARCHAR(1024) NULL,
    filming_locations TEXT NULL,
    language VARCHAR(100) NULL,
    synopsis LONGTEXT NULL,
    country_id BIGINT NULL,
    CONSTRAINT pk_film PRIMARY KEY (id),
    CONSTRAINT uk_film_tconst UNIQUE (tconst),
    CONSTRAINT fk_film_country FOREIGN KEY (country_id)
        REFERENCES country (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX ix_film_title (title),
    INDEX ix_film_release_year_start (release_year_start),
    INDEX ix_film_country_id (country_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE genre (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_genre PRIMARY KEY (id),
    CONSTRAINT uk_genre_name UNIQUE (normalized_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE film_genre (
    film_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    CONSTRAINT pk_film_genre PRIMARY KEY (film_id, genre_id),
    CONSTRAINT fk_film_genre_film FOREIGN KEY (film_id)
        REFERENCES film (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_film_genre_genre FOREIGN KEY (genre_id)
        REFERENCES genre (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX ix_film_genre_genre_id (genre_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE person (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nconst VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    birth_date DATE NULL,
    birth_date_source VARCHAR(64) NULL,
    birth_place VARCHAR(512) NULL,
    profile_url VARCHAR(1024) NULL,
    CONSTRAINT pk_person PRIMARY KEY (id),
    CONSTRAINT uk_person_nconst UNIQUE (nconst),
    INDEX ix_person_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE actor (
    person_id BIGINT NOT NULL,
    height_m DECIMAL(4,2) NULL,
    height_source VARCHAR(32) NULL,
    CONSTRAINT pk_actor PRIMARY KEY (person_id),
    CONSTRAINT fk_actor_person FOREIGN KEY (person_id)
        REFERENCES person (id) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE director (
    person_id BIGINT NOT NULL,
    CONSTRAINT pk_director PRIMARY KEY (person_id),
    CONSTRAINT fk_director_person FOREIGN KEY (person_id)
        REFERENCES person (id) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE film_director (
    film_id BIGINT NOT NULL,
    director_id BIGINT NOT NULL,
    CONSTRAINT pk_film_director PRIMARY KEY (film_id, director_id),
    CONSTRAINT fk_film_director_film FOREIGN KEY (film_id)
        REFERENCES film (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_film_director_director FOREIGN KEY (director_id)
        REFERENCES director (person_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX ix_film_director_director_id (director_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE film_cast (
    id BIGINT NOT NULL AUTO_INCREMENT,
    film_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    character_name TEXT NULL,
    is_principal BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_film_cast PRIMARY KEY (id),
    CONSTRAINT uk_film_cast_film_actor UNIQUE (film_id, actor_id),
    CONSTRAINT fk_film_cast_film FOREIGN KEY (film_id)
        REFERENCES film (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_film_cast_actor FOREIGN KEY (actor_id)
        REFERENCES actor (person_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX ix_film_cast_actor_id (actor_id),
    INDEX ix_film_cast_principal (is_principal, film_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
