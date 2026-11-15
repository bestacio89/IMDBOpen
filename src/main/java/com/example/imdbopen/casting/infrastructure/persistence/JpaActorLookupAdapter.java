package com.example.imdbopen.casting.infrastructure.persistence;

import com.example.imdbopen.actor.domain.Acteur;
import com.example.imdbopen.casting.application.port.out.ActorLookup;
import org.springframework.stereotype.Repository;
import persistence.repository.IActeurRepository;

import java.util.Optional;

@Repository
public class JpaActorLookupAdapter implements ActorLookup {
    private final IActeurRepository repository;

    public JpaActorLookupAdapter(IActeurRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Acteur> findByImdb(String imdbId) {
        return repository.findByImdb(imdbId);
    }
}
