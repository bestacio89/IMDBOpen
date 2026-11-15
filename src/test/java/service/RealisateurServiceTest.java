package service;

import com.example.imdbopen.person.domain.Personne;
import entities.business.personne.Realisateur;
import persistence.repository.IPersonneRepository;
import persistence.repository.IRealisateurRepository;
import web.model.dto.RealisateurDTO;
import exceptions.EntityNotFoundException;
import exceptions.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RealisateurServiceTest {
    @InjectMocks
    private RealisateurService service;

    @Mock
    private IRealisateurRepository repository;

    @Mock
    private IPersonneRepository personneRepository;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void findsAllDirectors() {
        when(repository.findAll()).thenReturn(List.of(director()));
        List<RealisateurDTO> result = service.findAll();
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getIdentite());
    }

    @Test
    void reusesExistingPersonWhenCreatingDirectorForExistingActorIdentity() {
        Personne person = person();
        when(personneRepository.findByNconst("nm0000001")).thenReturn(Optional.of(person));
        when(repository.save(any(Realisateur.class))).thenAnswer(invocation -> {
            Realisateur director = invocation.getArgument(0);
            director.setId(person.getId());
            return director;
        });

        RealisateurDTO dto = new RealisateurDTO();
        dto.setIdentite("Submitted Name");
        dto.setIdImdb("nm0000001");

        RealisateurDTO result = service.save(dto);

        assertEquals("John Doe", result.getIdentite());
        verify(personneRepository, never()).save(any(Personne.class));
    }

    @Test
    void updatesExistingPersonWithoutReplacingSharedIdentity() {
        Realisateur director = director();
        when(repository.findById(1L)).thenReturn(Optional.of(director));
        when(repository.save(director)).thenReturn(director);

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
        assertEquals("nm0000001", director.getPersonne().getNconst());
        assertEquals("Lyon", director.getPersonne().getLieuNaissance());
        verify(repository).save(director);
    }

    @Test
    void rejectsChangingAnExistingDirectorExternalIdentifier() {
        Realisateur director = director();
        when(repository.findById(1L)).thenReturn(Optional.of(director));
        RealisateurDTO dto = new RealisateurDTO();
        dto.setId(1L);
        dto.setIdImdb("nm0000002");

        assertThrows(exceptions.InvalidDataException.class, () -> service.update(dto));
        verify(repository, never()).save(any(Realisateur.class));
    }

    @Test
    void rejectsUpdateWhenDirectorDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        RealisateurDTO dto = new RealisateurDTO();
        dto.setId(1L);

        assertThrows(EntityNotFoundException.class, () -> service.update(dto));
        verify(repository, never()).save(any());
    }

    @Test
    void wrapsPersistenceFailureDuringUpdate() {
        Realisateur director = director();
        when(repository.findById(1L)).thenReturn(Optional.of(director));
        when(repository.save(director)).thenThrow(new RuntimeException("Database error"));

        RealisateurDTO dto = new RealisateurDTO();
        dto.setId(1L);
        dto.setIdentite("Updated Name");

        ServiceException exception = assertThrows(ServiceException.class, () -> service.update(dto));
        assertEquals("Failed to update Realisateur: Database error", exception.getMessage());
    }

    @Test
    void deletesExistingDirector() {
        when(repository.existsById(1L)).thenReturn(true);
        service.deleteById(1L);
        verify(repository).deleteById(1L);
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
