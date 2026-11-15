package com.example.imdbopen.film.infrastructure.persistence;

import com.example.imdbopen.film.application.port.out.FilmDirectorStore;
import com.example.imdbopen.film.domain.FilmDirector;
import com.example.imdbopen.film.domain.FilmDirectorId;
import org.springframework.stereotype.Repository;

@Repository
public class JpaFilmDirectorRepositoryAdapter implements FilmDirectorStore {
    private final FilmDirectorJpaRepository repository;

    public JpaFilmDirectorRepositoryAdapter(FilmDirectorJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsById(FilmDirectorId id) {
        return repository.existsById(id);
    }

    @Override
    public FilmDirector save(FilmDirector filmDirector) {
        return repository.save(filmDirector);
    }
}
