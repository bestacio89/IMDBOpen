package com.example.imdbopen.genre.application;
import com.example.imdbopen.genre.application.port.out.GenreStore;
import com.example.imdbopen.genre.domain.Genre;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Service
public class GenreService {
    private final GenreStore genreStore;
    public GenreService(GenreStore genreStore) { this.genreStore = genreStore; }
    @Transactional(readOnly = true)
    public List<Genre> findAll() { return genreStore.findAll(); }
    @Transactional(readOnly = true)
    public Optional<Genre> findById(Long id) { return genreStore.findById(id); }
    @Transactional
    public Genre save(Genre genre) { return genreStore.save(genre); }
    @Transactional
    public void deleteById(Long id) { genreStore.deleteById(id); }
    @Transactional
    public Genre findOrCreateGenre(String genreName) {
        return genreStore.findByName(genreName).orElseGet(() -> {
            Genre genre = new Genre(); genre.setName(genreName); return genreStore.save(genre);
        });
    }
}
