package com.example.imdbopen.film.infrastructure.persistence;

import com.example.imdbopen.film.domain.FilmDirector;
import com.example.imdbopen.film.domain.FilmDirectorId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilmDirectorJpaRepository extends JpaRepository<FilmDirector, FilmDirectorId> {
}
