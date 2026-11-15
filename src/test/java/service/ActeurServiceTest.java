package service;

import com.example.imdbopen.actor.domain.Acteur;
import com.example.imdbopen.person.domain.Personne;
import persistence.repository.IActeurRepository;
import persistence.repository.IPersonneRepository;
import web.model.dto.ActeurDTO;
import web.model.dto.PersonneDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActeurServiceTest {
    @InjectMocks
    private ActeurService acteurService;
    @Mock
    private IActeurRepository acteurRepository;
    @Mock
    private IPersonneRepository personneRepository;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void createsActorAndPersistsCanonicalPersonWhenAbsent() {
        PersonneDTO personDto = personDto("John Doe", "01-01-1995");
        ActeurDTO dto = actorDto(personDto, "nm0000001");
        when(personneRepository.findByNconst("nm0000001")).thenReturn(Optional.empty());
        when(personneRepository.save(any(Personne.class))).thenAnswer(invocation -> {
            Personne person = invocation.getArgument(0);
            person.setId(1L);
            return person;
        });
        when(acteurRepository.save(any(Acteur.class))).thenAnswer(invocation -> {
            Acteur actor = invocation.getArgument(0);
            actor.setId(actor.getPersonne().getId());
            return actor;
        });

        ActeurDTO result = acteurService.createActeur(dto);
        assertEquals("John Doe", result.getPersonne().getIdentite());
        assertEquals("01-01-1995", result.getPersonne().getDateNaissance());
        assertEquals("1.80m", result.getTaille());
        assertEquals("nm0000001", result.getIdImdb());
        verify(personneRepository).save(any(Personne.class));
    }

    @Test
    void reusesExistingPersonWhenCreatingActorForExistingDirectorIdentity() {
        Personne existingPerson = new Personne("John Doe", "1995-01-01", "Paris", null);
        existingPerson.setId(7L);
        existingPerson.setNconst("nm0000001");
        when(personneRepository.findByNconst("nm0000001")).thenReturn(Optional.of(existingPerson));
        when(acteurRepository.save(any(Acteur.class))).thenAnswer(invocation -> {
            Acteur actor = invocation.getArgument(0);
            actor.setId(existingPerson.getId());
            return actor;
        });
        ActeurDTO result = acteurService.createActeur(actorDto(
                personDto("A different submitted name", "1995-01-01"), "nm0000001"));
        assertEquals("John Doe", result.getPersonne().getIdentite());
        verify(personneRepository, never()).save(any(Personne.class));
    }

    @Test
    void updatesExistingPersonWithoutReplacingSharedIdentity() {
        Personne person = new Personne("John Doe", "1990-01-01", "Paris", "https://example.com");
        person.setId(1L);
        person.setNconst("nm0000001");
        Acteur actor = new Acteur();
        actor.setId(1L);
        actor.setPersonne(person);
        when(acteurRepository.findById(1L)).thenReturn(Optional.of(actor));
        when(acteurRepository.save(actor)).thenReturn(actor);
        ActeurDTO dto = actorDto(personDto("Updated Name", "1991-02-03"), "nm0000001");
        dto.setTaille("1.80m");

        ActeurDTO result = acteurService.updateActeur(1L, dto);
        assertSame(person, actor.getPersonne());
        assertEquals("Updated Name", result.getPersonne().getIdentite());
        assertEquals("1991-02-03", result.getPersonne().getDateNaissance());
        verify(acteurRepository).save(actor);
    }

    @Test
    void rejectsInvalidActorData() {
        assertThrows(InvalidDataException.class, () -> acteurService.createActeur(new ActeurDTO()));
        verify(acteurRepository, never()).save(any(Acteur.class));
    }

    @Test
    void rejectsChangingAnExistingActorExternalIdentifier() {
        Personne person = new Personne("John Doe", "1990-01-01", "Paris", null);
        person.setId(1L);
        person.setNconst("nm0000001");
        Acteur actor = new Acteur();
        actor.setId(1L);
        actor.setPersonne(person);
        when(acteurRepository.findById(1L)).thenReturn(Optional.of(actor));
        ActeurDTO dto = actorDto(personDto("John Doe", "1990-01-01"), "nm0000002");
        assertThrows(InvalidDataException.class, () -> acteurService.updateActeur(1L, dto));
        verify(acteurRepository, never()).save(any(Acteur.class));
    }

    @Test
    void rejectsUpdateForMissingActor() {
        when(acteurRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> acteurService.updateActeur(1L, new ActeurDTO()));
        verify(acteurRepository, never()).save(any(Acteur.class));
    }

    @Test
    void deletesExistingActor() {
        when(acteurRepository.existsById(1L)).thenReturn(true);
        acteurService.deleteActeur(1L);
        verify(acteurRepository).deleteById(1L);
    }

    @Test
    void rejectsDeletionForMissingActor() {
        when(acteurRepository.existsById(1L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> acteurService.deleteActeur(1L));
        verify(acteurRepository, never()).deleteById(anyLong());
    }

    private ActeurDTO actorDto(PersonneDTO person, String nconst) {
        ActeurDTO dto = new ActeurDTO();
        dto.setPersonne(person);
        dto.setIdImdb(nconst);
        dto.setTaille("1.80m");
        return dto;
    }

    private PersonneDTO personDto(String name, String birthDate) {
        PersonneDTO dto = new PersonneDTO();
        dto.setIdentite(name);
        dto.setDateNaissance(birthDate);
        dto.setLieuNaissance("Paris, France");
        dto.setUrl("https://example.com/profile");
        return dto;
    }
}
