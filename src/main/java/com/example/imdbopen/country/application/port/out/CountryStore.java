package com.example.imdbopen.country.application.port.out;

import com.example.imdbopen.country.domain.Pays;

import java.util.List;
import java.util.Optional;

public interface CountryStore {
    List<Pays> findAll();

    Optional<Pays> findById(Long id);

    Pays save(Pays pays);

    void deleteById(Long id);

    Optional<Pays> findByName(String name);
}
