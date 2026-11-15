package com.example.imdbopen.casting.infrastructure.persistence;

import com.example.imdbopen.casting.application.port.out.FilmLookup;
import com.example.imdbopen.film.domain.Film;
import org.springframework.stereotype.Repository;
import persistence.repository.IFilmRepository;

import java.util.Optional;

@Repository
public class JpaFilmLookupAdapter implements FilmLookup {
    private final IFilmRepository repository;

    public JpaFilmLookupAdapter(IFilmRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Film> findByImdb(String imdbId) {
        return repository.findByImdb(imdbId);
    }
}
