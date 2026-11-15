package com.example.imdbopen.director.application.port.out;

import com.example.imdbopen.director.domain.Realisateur;

import java.util.List;
import java.util.Optional;

public interface DirectorStore {
    List<Realisateur> findAll();
    Optional<Realisateur> findById(Long id);
    Optional<Realisateur> findByIdImdb(String idImdb);
    Realisateur save(Realisateur director);
    boolean existsById(Long id);
    void deleteById(Long id);
}
