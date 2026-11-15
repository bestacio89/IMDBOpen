package com.example.imdbopen.film.infrastructure.persistence;

import com.example.imdbopen.director.application.port.out.DirectorStore;
import com.example.imdbopen.director.domain.Realisateur;
import com.example.imdbopen.film.application.port.out.DirectorLookup;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaDirectorLookupAdapter implements DirectorLookup {
    private final DirectorStore directorStore;

    public JpaDirectorLookupAdapter(DirectorStore directorStore) {
        this.directorStore = directorStore;
    }

    @Override
    public Optional<Realisateur> findByImdb(String imdbId) {
        return directorStore.findByIdImdb(imdbId);
    }
}
