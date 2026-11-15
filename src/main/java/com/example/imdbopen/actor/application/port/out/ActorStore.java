package com.example.imdbopen.actor.application.port.out;

import com.example.imdbopen.actor.domain.Acteur;

import java.util.Optional;

public interface ActorStore {
    Optional<Acteur> findById(Long id);
    boolean existsById(Long id);
    Acteur save(Acteur actor);
    void deleteById(Long id);
}
