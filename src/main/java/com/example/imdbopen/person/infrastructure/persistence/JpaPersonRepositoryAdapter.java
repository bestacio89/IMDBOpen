package com.example.imdbopen.person.infrastructure.persistence;

import com.example.imdbopen.person.application.port.out.PersonStore;
import com.example.imdbopen.person.domain.Personne;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaPersonRepositoryAdapter implements PersonStore {
    private final PersonJpaRepository repository;

    public JpaPersonRepositoryAdapter(PersonJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Personne> findByNconst(String nconst) {
        return repository.findByNconst(nconst);
    }

    @Override
    public Personne save(Personne person) {
        return repository.save(person);
    }
}
