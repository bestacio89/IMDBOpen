package com.example.imdbopen.film.application;

import com.example.imdbopen.director.domain.Realisateur;
import com.example.imdbopen.film.application.port.out.DirectorLookup;
import com.example.imdbopen.film.application.port.out.FilmDirectorStore;
import com.example.imdbopen.film.application.port.out.FilmStore;
import com.example.imdbopen.film.domain.Film;
import com.example.imdbopen.film.domain.FilmDirector;
import com.example.imdbopen.film.domain.FilmDirectorId;
import exceptions.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmDirectorServiceTest {
    @Mock
    private FilmStore filmStore;
    @Mock
    private DirectorLookup directorLookup;
    @Mock
    private FilmDirectorStore filmDirectorStore;
    @InjectMocks
    private FilmDirectorService service;

    @Test
    void createsFilmDirectorAssociation() {
        Film film = new Film();
        film.setId(1L);
        Realisateur director = new Realisateur();
        director.setId(2L);
        when(filmStore.findByImdb("tt0000001")).thenReturn(Optional.of(film));
        when(directorLookup.findByImdb("nm0000001")).thenReturn(Optional.of(director));
        when(filmDirectorStore.existsById(new FilmDirectorId(1L, 2L))).thenReturn(false);

        assertTrue(service.assignDirectorToFilm("tt0000001", "nm0000001"));

        verify(filmDirectorStore).save(any(FilmDirector.class));
    }

    @Test
    void doesNotDuplicateExistingAssociation() {
        Film film = new Film();
        film.setId(1L);
        Realisateur director = new Realisateur();
        director.setId(2L);
        when(filmStore.findByImdb("tt0000001")).thenReturn(Optional.of(film));
        when(directorLookup.findByImdb("nm0000001")).thenReturn(Optional.of(director));
        when(filmDirectorStore.existsById(new FilmDirectorId(1L, 2L))).thenReturn(true);

        assertFalse(service.assignDirectorToFilm("tt0000001", "nm0000001"));

        verify(filmDirectorStore, never()).save(any(FilmDirector.class));
    }

    @Test
    void rejectsUnknownFilm() {
        when(filmStore.findByImdb("tt-missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.assignDirectorToFilm("tt-missing", "nm0000001"));
        verifyNoInteractions(directorLookup, filmDirectorStore);
    }

    @Test
    void rejectsUnknownDirector() {
        Film film = new Film();
        film.setId(1L);
        when(filmStore.findByImdb("tt0000001")).thenReturn(Optional.of(film));
        when(directorLookup.findByImdb("nm-missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.assignDirectorToFilm("tt0000001", "nm-missing"));
        verifyNoInteractions(filmDirectorStore);
    }
}
