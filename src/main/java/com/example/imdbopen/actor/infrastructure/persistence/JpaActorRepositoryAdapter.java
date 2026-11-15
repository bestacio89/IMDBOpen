package com.example.imdbopen.actor.infrastructure.persistence;

import com.example.imdbopen.actor.application.port.out.ActorStore;
import com.example.imdbopen.actor.domain.Acteur;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaActorRepositoryAdapter implements ActorStore {
    private final ActorJpaRepository repository;

    public JpaActorRepositoryAdapter(ActorJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Acteur> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Acteur> findByImdb(String imdbId) {
        return repository.findByImdb(imdbId);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public Acteur save(Acteur actor) {
        return repository.save(actor);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
