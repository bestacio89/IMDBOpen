package com.example.imdbopen.genre.application;
import com.example.imdbopen.genre.application.port.out.GenreStore;
import com.example.imdbopen.genre.domain.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;
class GenreServiceTest {
    @InjectMocks private GenreService genreService;
    @Mock private GenreStore genreStore;
    @BeforeEach void setUp() { MockitoAnnotations.openMocks(this); }
    @Test void saveDelegatesToGenreStore() {
        Genre genre = new Genre(); when(genreStore.save(genre)).thenReturn(genre);
        assertSame(genre, genreService.save(genre)); verify(genreStore).save(genre);
    }
    @Test void findOrCreateReturnsExistingGenre() {
        Genre existing = new Genre(null, "Comedy");
        when(genreStore.findByName("Comedy")).thenReturn(Optional.of(existing));
        assertSame(existing, genreService.findOrCreateGenre("Comedy"));
        verify(genreStore, never()).save(any());
    }
    @Test void findOrCreatePersistsGenreWhenMissing() {
        Genre created = new Genre(null, "Drama");
        when(genreStore.findByName("Drama")).thenReturn(Optional.empty());
        when(genreStore.save(any(Genre.class))).thenReturn(created);
        assertSame(created, genreService.findOrCreateGenre("Drama"));
        verify(genreStore).save(argThat(g -> "Drama".equals(g.getName())));
    }
}
