package com.example.imdbopen.film.infrastructure.persistence;

import com.example.imdbopen.film.application.port.out.FilmStore;
import com.example.imdbopen.film.domain.Film;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaFilmRepositoryAdapter implements FilmStore {
    private final FilmJpaRepository repository;

    public JpaFilmRepositoryAdapter(FilmJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Film> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Film> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Film> findByImdb(String imdbId) {
        return repository.findByImdb(imdbId);
    }

    @Override
    public List<Film> findByNomContainingIgnoreCase(String name) {
        return repository.findByNomContainingIgnoreCase(name);
    }

    @Override
    public Film save(Film film) {
        return repository.save(film);
    }

    @Override
    public void saveAll(List<Film> films) {
        repository.saveAll(films);
    }

    @Override
    public Film saveAndFlush(Film film) {
        return repository.saveAndFlush(film);
    }

    @Override
    public void delete(Film film) {
        repository.delete(film);
    }
}
