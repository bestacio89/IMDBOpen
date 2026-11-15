package com.example.imdbopen.casting.infrastructure.persistence;

import com.example.imdbopen.actor.application.port.out.ActorStore;
import com.example.imdbopen.actor.domain.Acteur;
import com.example.imdbopen.casting.application.port.out.ActorLookup;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaActorLookupAdapter implements ActorLookup {
    private final ActorStore actorStore;

    public JpaActorLookupAdapter(ActorStore actorStore) {
        this.actorStore = actorStore;
    }

    @Override
    public Optional<Acteur> findByImdb(String imdbId) {
        return actorStore.findByImdb(imdbId);
    }
}
