package com.example.imdbopen.film.application.port.out;

import com.example.imdbopen.genre.domain.Genre;

import java.util.Optional;

public interface GenreCatalog {
    Optional<Genre> findByName(String name);
    Genre save(Genre genre);
}
