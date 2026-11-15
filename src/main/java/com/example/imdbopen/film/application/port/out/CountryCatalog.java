package com.example.imdbopen.film.application.port.out;

import com.example.imdbopen.country.domain.Pays;

import java.util.Optional;

public interface CountryCatalog {
    Optional<Pays> findById(Long id);
    Optional<Pays> findByName(String name);
    Pays save(Pays country);
}
