package com.example.imdbopen.actor.infrastructure.persistence;

import com.example.imdbopen.actor.domain.Acteur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActorJpaRepository extends JpaRepository<Acteur, Long> {
}
