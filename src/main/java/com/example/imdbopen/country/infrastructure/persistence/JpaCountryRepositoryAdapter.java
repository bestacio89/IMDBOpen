package com.example.imdbopen.country.infrastructure.persistence;

import com.example.imdbopen.country.application.port.out.CountryStore;
import com.example.imdbopen.country.domain.Pays;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaCountryRepositoryAdapter implements CountryStore {
    private final CountryJpaRepository repository;

    public JpaCountryRepositoryAdapter(CountryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Pays> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Pays> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Pays save(Pays pays) {
        return repository.save(pays);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Pays> findByName(String name) {
        return repository.findByName(name);
    }
}
