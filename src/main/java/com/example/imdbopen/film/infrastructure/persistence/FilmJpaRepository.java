package com.example.imdbopen.film.infrastructure.persistence;

import com.example.imdbopen.film.domain.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FilmJpaRepository extends JpaRepository<Film, Long> {
    @Query("SELECT f FROM Film f WHERE f.tconst = :imdb")
    Optional<Film> findByImdb(@Param("imdb") String imdb);

    @Query("SELECT f FROM Film f WHERE LOWER(f.title) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Film> findByNomContainingIgnoreCase(@Param("name") String name);
}
