package com.example.imdbopen.film.application;

import com.example.imdbopen.country.domain.Pays;
import com.example.imdbopen.film.contracts.CountryReference;
import com.example.imdbopen.film.application.port.out.CountryCatalog;
import com.example.imdbopen.film.application.port.out.FilmStore;
import com.example.imdbopen.film.application.port.out.GenreCatalog;
import com.example.imdbopen.film.domain.Film;
import com.example.imdbopen.genre.domain.Genre;
import exceptions.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.imdbopen.film.contracts.FilmDTO;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {
    @Mock
    private FilmStore filmStore;
    @Mock
    private GenreCatalog genreCatalog;
    @Mock
    private CountryCatalog countryCatalog;
    @InjectMocks
    private FilmService filmService;

    @Test
    void saveDelegatesToFilmStore() {
        Film film = new Film();
        when(filmStore.saveAndFlush(film)).thenReturn(film);
        filmService.save(film);
        verify(filmStore).saveAndFlush(film);
    }

    @Test
    void findsFilmByExternalIdentifier() {
        Film film = film("tt0000001", "Inception", "2010");
        when(filmStore.findByImdb("tt0000001")).thenReturn(Optional.of(film));
        FilmDTO result = filmService.findFilmByImdb("tt0000001").orElseThrow();
        assertEquals("Inception", result.getNom());
    }

    @Test
    void findsFilmsByName() {
        when(filmStore.findByNomContainingIgnoreCase("Inception"))
                .thenReturn(List.of(film("tt0000001", "Inception", "2010")));
        assertEquals(1, filmService.findFilmsByName("Inception").size());
    }

    @Test
    void rejectsNameSearchWithoutMatches() {
        when(filmStore.findByNomContainingIgnoreCase("Missing")).thenReturn(List.of());
        assertThrows(EntityNotFoundException.class, () -> filmService.findFilmsByName("Missing"));
    }

    @Test
    void createsFilmUsingExistingCountryAndGenres() {
        Pays country = new Pays(1L, "France");
        Genre drama = new Genre(1L, "Drama");
        Genre comedy = new Genre(2L, "Comedy");
        when(countryCatalog.findById(1L)).thenReturn(Optional.of(country));
        when(genreCatalog.findByName("Drama")).thenReturn(Optional.of(drama));
        when(genreCatalog.findByName("Comedy")).thenReturn(Optional.of(comedy));
        when(filmStore.save(any(Film.class))).thenAnswer(invocation -> {
            Film saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        FilmDTO dto = new FilmDTO();
        dto.setImdb("tt0000010");
        dto.setNom("Test Film");
        dto.setAnnee("2026");
        dto.setPays(CountryReference.fromEntity(country));
        dto.setGenres("Drama, Comedy");

        FilmDTO result = filmService.createFilm(dto);

        assertEquals(10L, result.getId());
        assertEquals("France", result.getPays().getName());
        ArgumentCaptor<Film> filmCaptor = ArgumentCaptor.forClass(Film.class);
        verify(filmStore).save(filmCaptor.capture());
        assertEquals(2, filmCaptor.getValue().getGenresl().size());
        verify(genreCatalog).findByName("Drama");
        verify(genreCatalog).findByName("Comedy");
    }

    @Test
    void createsMissingCountryAndGenreOnDemand() {
        when(countryCatalog.findByName("France")).thenReturn(Optional.empty());
        when(countryCatalog.save(any(Pays.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(genreCatalog.findByName("Drama")).thenReturn(Optional.empty());
        when(genreCatalog.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(filmStore.save(any(Film.class))).thenAnswer(invocation -> invocation.getArgument(0));
        FilmDTO dto = new FilmDTO();
        dto.setImdb("tt0000011");
        dto.setNom("New Film");
        CountryReference country = new CountryReference();
        country.setName("France");
        dto.setPays(country);
        dto.setGenres("Drama");

        filmService.createFilm(dto);

        verify(countryCatalog).save(any(Pays.class));
        verify(genreCatalog).save(any(Genre.class));
    }

    @Test
    void updatesExistingFilmAndSynchronizesAssociations() {
        Film existing = film("tt0000001", "Old Title", "2010");
        existing.setId(1L);
        Pays country = new Pays(2L, "Spain");
        Genre genre = new Genre(2L, "Thriller");
        when(filmStore.findById(1L)).thenReturn(Optional.of(existing));
        when(countryCatalog.findById(2L)).thenReturn(Optional.of(country));
        when(genreCatalog.findByName("Thriller")).thenReturn(Optional.of(genre));
        when(filmStore.save(existing)).thenReturn(existing);
        FilmDTO dto = new FilmDTO();
        dto.setNom("New Title");
        dto.setAnnee("2011");
        dto.setPays(country);
        dto.setGenres("Thriller");

        FilmDTO result = filmService.updateFilm(1L, dto);

        assertEquals("New Title", result.getNom());
        assertEquals("Spain", result.getPays().getName());
        assertEquals(1, existing.getGenresl().size());
        verify(filmStore).save(existing);
    }

    @Test
    void deletesExistingFilm() {
        Film film = film("tt0000001", "Inception", "2010");
        when(filmStore.findById(1L)).thenReturn(Optional.of(film));
        filmService.deleteFilm(1L);
        verify(filmStore).delete(film);
    }

    @Test
    void rejectsDeletionOfMissingFilm() {
        when(filmStore.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> filmService.deleteFilm(1L));
        verify(filmStore, never()).delete(any(Film.class));
    }

    private Film film(String imdb, String title, String year) {
        Film film = new Film();
        film.setImdb(imdb);
        film.setNom(title);
        film.setAnnee(year);
        return film;
    }
}
