package service;

import com.example.imdbopen.genre.domain.Genre;
import persistence.repository.IGenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GenreService {
    private final IGenreRepository genreRepository;

    public GenreService(IGenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Transactional(readOnly = true)
    public List<Genre> findAll() { return genreRepository.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Genre> findById(Long id) { return genreRepository.findById(id); }

    @Transactional
    public Genre save(Genre genre) { return genreRepository.save(genre); }

    @Transactional
    public void deleteById(Long id) { genreRepository.deleteById(id); }

    @Transactional
    public Genre findOrCreateGenre(String genreName) {
        return genreRepository.findByName(genreName).orElseGet(() -> {
            Genre genre = new Genre();
            genre.setName(genreName);
            return genreRepository.save(genre);
        });
    }
}
