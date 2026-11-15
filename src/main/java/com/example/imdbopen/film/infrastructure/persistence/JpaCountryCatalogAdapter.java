package com.example.imdbopen.film.infrastructure.persistence;

import com.example.imdbopen.country.application.port.out.CountryStore;
import com.example.imdbopen.country.domain.Pays;
import com.example.imdbopen.film.application.port.out.CountryCatalog;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaCountryCatalogAdapter implements CountryCatalog {
    private final CountryStore countryStore;

    public JpaCountryCatalogAdapter(CountryStore countryStore) {
        this.countryStore = countryStore;
    }

    @Override
    public Optional<Pays> findById(Long id) {
        return countryStore.findById(id);
    }

    @Override
    public Optional<Pays> findByName(String name) {
        return countryStore.findByName(name);
    }

    @Override
    public Pays save(Pays country) {
        return countryStore.save(country);
    }
}
