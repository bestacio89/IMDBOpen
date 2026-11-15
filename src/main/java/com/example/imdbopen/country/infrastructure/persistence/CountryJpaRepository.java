package com.example.imdbopen.country.infrastructure.persistence;

import com.example.imdbopen.country.domain.Pays;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryJpaRepository extends JpaRepository<Pays, Long> {
    Optional<Pays> findByName(String name);
}
