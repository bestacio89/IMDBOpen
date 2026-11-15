package com.example.imdbopen.film.application.port.out;

import com.example.imdbopen.director.domain.Realisateur;

import java.util.Optional;

public interface DirectorLookup {
    Optional<Realisateur> findByImdb(String imdbId);
}
