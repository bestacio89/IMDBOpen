package com.example.imdbopen.director.application;

import com.example.imdbopen.director.application.port.out.DirectorStore;
import com.example.imdbopen.director.domain.Realisateur;
import com.example.imdbopen.person.application.port.out.PersonStore;
import com.example.imdbopen.person.domain.Personne;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import exceptions.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import web.model.dto.RealisateurDTO;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealisateurServiceTest {
    @Mock
    private DirectorStore directorStore;
    @Mock
    private PersonStore personStore;
    @InjectMocks
    private RealisateurService service;

    @Test
    void findsAllDirectors() {
        when(directorStore.findAll()).thenReturn(List.of(director()));
        List<RealisateurDTO> result = service.findAll();
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getIdentite());
    }

    @Test
    void findsDirectorById() {
        when(directorStore.findById(1L)).thenReturn(Optional.of(director()));
        assertEquals("John Doe", service.findById(1L).orElseThrow().getIdentite());
    }

    @Test
    void findsDirectorByImdbIdentifier() {
        when(directorStore.findByIdImdb("nm0000001")).thenReturn(Optional.of(director()));
        assertEquals(1L, service.findByIdImdb("nm0000001").orElseThrow().getId());
    }

    @Test
    void reusesExistingPersonWhenCreatingDirectorForExistingActorIdentity() {
        Personne person = person();
        when(personStore.findByNconst("nm0000001")).thenReturn(Optional.of(person));
        when(directorStore.save(any(Realisateur.class))).thenAnswer(invocation -> {
            Realisateur director = invocation.getArgument(0);
            director.setId(person.getId());
            return director;
        });
        RealisateurDTO dto = new RealisateurDTO();
        dto.setIdentite("Submitted Name");
        dto.setIdImdb("nm0000001");

        RealisateurDTO result = service.save(dto);

        assertEquals("John Doe", result.getIdentite());
        ArgumentCaptor<Realisateur> captor = ArgumentCaptor.forClass(Realisateur.class);
        verify(directorStore).save(captor.capture());
        assertSame(person, captor.getValue().getPersonne());
        verify(personStore, never()).save(any(Personne.class));
    }

    @Test
    void createsNewPersonThroughDirectorPersistenceCascadeWhenIdentityIsUnknown() {
        when(personStore.findByNconst("nm0000002")).thenReturn(Optional.empty());
        when(directorStore.save(any(Realisateur.class))).thenAnswer(invocation -> {
            Realisateur director = invocation.getArgument(0);
            director.setId(2L);
            return director;
        });
        RealisateurDTO dto = new RealisateurDTO();
        dto.setIdentite("New Director");
        dto.setDateNaissance("1980-01-01");
        dto.setLieuNaissance("Lyon");
        dto.setUrl("https://example.com/director");
        dto.setIdImdb("nm0000002");

        RealisateurDTO result = service.save(dto);

        assertEquals("New Director", result.getIdentite());
        assertEquals("nm0000002", result.getIdImdb());
        verify(personStore, never()).save(any(Personne.class));
    }

    @Test
    void rejectsDirectorWithoutExternalIdentifier() {
        assertThrows(InvalidDataException.class, () -> service.save(new RealisateurDTO()));
        verify(directorStore, never()).save(any(Realisateur.class));
    }

    @Test
    void updatesExistingPersonWithoutReplacingSharedIdentity() {
        Realisateur director = director();
        when(directorStore.findById(1L)).thenReturn(Optional.of(director));
        when(directorStore.save(director)).thenReturn(director);
        RealisateurDTO dto = new RealisateurDTO();
        dto.setId(1L);
        dto.setIdentite("Updated Name");
        dto.setIdImdb("nm0000001");
        dto.setDateNaissance("1990-01-01");
        dto.setLieuNaissance("Lyon");
        dto.setUrl("https://example.com/profile");

        RealisateurDTO result = service.update(dto);

        assertEquals("Updated Name", result.getIdentite());
        assertEquals("nm0000001", director.getPersonne().getNconst());
        assertEquals("Lyon", director.getPersonne().getLieuNaissance());
        verify(directorStore).save(director);
    }

    @Test
    void rejectsChangingAnExistingDirectorExternalIdentifier() {
        Realisateur director = director();
        when(directorStore.findById(1L)).thenReturn(Optional.of(director));
        RealisateurDTO dto = new RealisateurDTO();
        dto.setId(1L);
        dto.setIdImdb("nm0000002");
        assertThrows(InvalidDataException.class, () -> service.update(dto));
        verify(directorStore, never()).save(any(Realisateur.class));
    }

    @Test
    void rejectsUpdateWhenDirectorDoesNotExist() {
        when(directorStore.findById(1L)).thenReturn(Optional.empty());
        RealisateurDTO dto = new RealisateurDTO();
        dto.setId(1L);
        assertThrows(EntityNotFoundException.class, () -> service.update(dto));
        verify(directorStore, never()).save(any());
    }

    @Test
    void wrapsPersistenceFailureDuringUpdate() {
        Realisateur director = director();
        when(directorStore.findById(1L)).thenReturn(Optional.of(director));
        when(directorStore.save(director)).thenThrow(new RuntimeException("Database error"));
        RealisateurDTO dto = new RealisateurDTO();
        dto.setId(1L);
        dto.setIdentite("Updated Name");
        ServiceException exception = assertThrows(ServiceException.class, () -> service.update(dto));
        assertEquals("Failed to update Realisateur: Database error", exception.getMessage());
    }

    @Test
    void deletesExistingDirector() {
        when(directorStore.existsById(1L)).thenReturn(true);
        service.deleteById(1L);
        verify(directorStore).deleteById(1L);
    }

    @Test
    void rejectsDeletionForMissingDirector() {
        when(directorStore.existsById(1L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.deleteById(1L));
        verify(directorStore, never()).deleteById(anyLong());
    }

    private Realisateur director() {
        Realisateur director = new Realisateur();
        director.setId(1L);
        director.setPersonne(person());
        return director;
    }

    private Personne person() {
        Personne person = new Personne("John Doe", "1990-01-01", "Paris, France", "https://example.com/profile");
        person.setId(1L);
        person.setNconst("nm0000001");
        return person;
    }
}
