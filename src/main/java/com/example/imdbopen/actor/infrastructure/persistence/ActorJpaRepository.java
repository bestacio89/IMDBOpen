package com.example.imdbopen.actor.infrastructure.persistence;

import com.example.imdbopen.actor.domain.Acteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ActorJpaRepository extends JpaRepository<Acteur, Long> {
    @Query("SELECT a FROM Acteur a WHERE a.personne.nconst = :imdbId")
    Optional<Acteur> findByImdb(@Param("imdbId") String imdbId);
}
