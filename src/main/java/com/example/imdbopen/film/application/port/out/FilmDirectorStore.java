package com.example.imdbopen.film.application.port.out;

import com.example.imdbopen.film.domain.FilmDirector;
import com.example.imdbopen.film.domain.FilmDirectorId;

public interface FilmDirectorStore {
    boolean existsById(FilmDirectorId id);
    FilmDirector save(FilmDirector filmDirector);
}
