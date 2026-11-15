package com.example.imdbopen.casting.infrastructure.persistence;

import com.example.imdbopen.casting.application.port.out.FilmLookup;
import com.example.imdbopen.film.application.port.out.FilmStore;
import com.example.imdbopen.film.domain.Film;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaFilmLookupAdapter implements FilmLookup {
    private final FilmStore filmStore;

    public JpaFilmLookupAdapter(FilmStore filmStore) {
        this.filmStore = filmStore;
    }

    @Override
    public Optional<Film> findByImdb(String imdbId) {
        return filmStore.findByImdb(imdbId);
    }
}
