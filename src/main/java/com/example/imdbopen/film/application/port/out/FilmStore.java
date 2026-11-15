package com.example.imdbopen.film.application.port.out;

import com.example.imdbopen.film.domain.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStore {
    List<Film> findAll();
    Optional<Film> findById(Long id);
    Optional<Film> findByImdb(String imdbId);
    List<Film> findByNomContainingIgnoreCase(String name);
    Film save(Film film);
    void saveAll(List<Film> films);
    Film saveAndFlush(Film film);
    void delete(Film film);
}
