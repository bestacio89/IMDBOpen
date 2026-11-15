package com.example.imdbopen.film.infrastructure.persistence;

import com.example.imdbopen.film.application.port.out.GenreCatalog;
import com.example.imdbopen.genre.application.port.out.GenreStore;
import com.example.imdbopen.genre.domain.Genre;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaGenreCatalogAdapter implements GenreCatalog {
    private final GenreStore genreStore;

    public JpaGenreCatalogAdapter(GenreStore genreStore) {
        this.genreStore = genreStore;
    }

    @Override
    public Optional<Genre> findByName(String name) {
        return genreStore.findByName(name);
    }

    @Override
    public Genre save(Genre genre) {
        return genreStore.save(genre);
    }
}
