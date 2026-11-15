package com.example.imdbopen.genre.infrastructure.persistence;
import com.example.imdbopen.genre.application.port.out.GenreStore;
import com.example.imdbopen.genre.domain.Genre;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public class JpaGenreRepositoryAdapter implements GenreStore {
    private final GenreJpaRepository repository;
    public JpaGenreRepositoryAdapter(GenreJpaRepository repository) { this.repository = repository; }
    @Override public List<Genre> findAll() { return repository.findAll(); }
    @Override public Optional<Genre> findById(Long id) { return repository.findById(id); }
    @Override public Genre save(Genre genre) { return repository.save(genre); }
    @Override public void deleteById(Long id) { repository.deleteById(id); }
    @Override public Optional<Genre> findByName(String name) { return repository.findByName(name); }
}
