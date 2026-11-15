package com.example.imdbopen.genre.application.port.out;
import com.example.imdbopen.genre.domain.Genre;
import java.util.List;
import java.util.Optional;
public interface GenreStore {
    List<Genre> findAll();
    Optional<Genre> findById(Long id);
    Genre save(Genre genre);
    void deleteById(Long id);
    Optional<Genre> findByName(String name);
}
