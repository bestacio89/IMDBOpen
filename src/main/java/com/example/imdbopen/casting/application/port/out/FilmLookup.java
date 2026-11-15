package com.example.imdbopen.casting.application.port.out;

import com.example.imdbopen.film.domain.Film;

import java.util.Optional;

public interface FilmLookup {
    Optional<Film> findByImdb(String imdbId);
}
