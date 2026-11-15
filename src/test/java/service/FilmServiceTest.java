package service;

import com.example.imdbopen.country.application.port.out.CountryStore;
import com.example.imdbopen.film.domain.Film;
import com.example.imdbopen.genre.application.port.out.GenreStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import persistence.repository.IFilmRepository;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FilmServiceTest {
    @InjectMocks
    private FilmService filmService;

    @Mock
    private IFilmRepository filmRepository;

    @Mock
    private GenreStore genreStore;

    @Mock
    private CountryStore countryStore;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveDelegatesToFilmRepository() {
        Film film = new Film();
        when(filmRepository.saveAndFlush(film)).thenReturn(film);

        filmService.save(film);

        verify(filmRepository, times(1)).saveAndFlush(film);
    }
}
