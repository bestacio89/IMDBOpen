package com.example.imdbopen.person.infrastructure.persistence;

import com.example.imdbopen.person.domain.Personne;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonJpaRepository extends JpaRepository<Personne, Long> {
    Optional<Personne> findByNconst(String nconst);
}
