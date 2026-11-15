package com.example.imdbopen.casting.application.port.out;

import com.example.imdbopen.actor.domain.Acteur;

import java.util.Optional;

public interface ActorLookup {
    Optional<Acteur> findByImdb(String imdbId);
}
