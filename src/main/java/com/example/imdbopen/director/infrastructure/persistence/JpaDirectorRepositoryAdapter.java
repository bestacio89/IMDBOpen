package com.example.imdbopen.director.infrastructure.persistence;

import com.example.imdbopen.director.application.port.out.DirectorStore;
import com.example.imdbopen.director.domain.Realisateur;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaDirectorRepositoryAdapter implements DirectorStore {
    private final DirectorJpaRepository repository;

    public JpaDirectorRepositoryAdapter(DirectorJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Realisateur> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Realisateur> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Realisateur> findByIdImdb(String idImdb) {
        return repository.findByIdImdb(idImdb);
    }

    @Override
    public Realisateur save(Realisateur director) {
        return repository.save(director);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
