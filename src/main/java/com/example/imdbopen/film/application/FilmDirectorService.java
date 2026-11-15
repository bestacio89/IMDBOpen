package com.example.imdbopen.film.application;

import com.example.imdbopen.director.domain.Realisateur;
import com.example.imdbopen.film.application.port.out.DirectorLookup;
import com.example.imdbopen.film.application.port.out.FilmDirectorStore;
import com.example.imdbopen.film.application.port.out.FilmStore;
import com.example.imdbopen.film.domain.Film;
import com.example.imdbopen.film.domain.FilmDirector;
import com.example.imdbopen.film.domain.FilmDirectorId;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FilmDirectorService {
    private final FilmStore filmStore;
    private final DirectorLookup directorLookup;
    private final FilmDirectorStore filmDirectorStore;

    public FilmDirectorService(FilmStore filmStore, DirectorLookup directorLookup,
                               FilmDirectorStore filmDirectorStore) {
        this.filmStore = filmStore;
        this.directorLookup = directorLookup;
        this.filmDirectorStore = filmDirectorStore;
    }

    @Transactional
    public boolean assignDirectorToFilm(String filmImdbId, String directorImdbId) {
        if (filmImdbId == null || filmImdbId.isBlank()
                || directorImdbId == null || directorImdbId.isBlank()) {
            throw new InvalidDataException("Film and director IMDb identifiers are required");
        }
        Film film = filmStore.findByImdb(filmImdbId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Film not found with IMDb identifier: " + filmImdbId));
        Realisateur director = directorLookup.findByImdb(directorImdbId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Director not found with IMDb identifier: " + directorImdbId));

        FilmDirectorId associationId = new FilmDirectorId(film.getId(), director.getId());
        if (filmDirectorStore.existsById(associationId)) {
            return false;
        }
        filmDirectorStore.save(new FilmDirector(film, director));
        return true;
    }
}
