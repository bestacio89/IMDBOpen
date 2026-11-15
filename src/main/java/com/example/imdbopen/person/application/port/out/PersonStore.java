package com.example.imdbopen.person.application.port.out;

import com.example.imdbopen.person.domain.Personne;

import java.util.Optional;

public interface PersonStore {
    Optional<Personne> findByNconst(String nconst);
    Personne save(Personne person);
}
