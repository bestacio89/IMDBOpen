package com.example.imdbopen.actor.application;

import com.example.imdbopen.actor.application.port.out.ActorStore;
import com.example.imdbopen.actor.domain.Acteur;
import com.example.imdbopen.person.application.port.out.PersonStore;
import com.example.imdbopen.person.domain.Personne;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.imdbopen.actor.contracts.ActeurDTO;
import com.example.imdbopen.person.contracts.PersonneDTO;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActeurServiceTest {
    @Mock
    private ActorStore actorStore;
    @Mock
    private PersonStore personStore;
    @InjectMocks
    private ActeurService acteurService;

    @Test
    void findsActorByExternalIdentifier() {
        Acteur actor = new Acteur();
        actor.setPersonne(new Personne("John Doe", "1990-01-01", "Paris", null));
        when(actorStore.findByImdb("nm0000001")).thenReturn(Optional.of(actor));

        assertEquals("nm0000001", acteurService.findByImdb("nm0000001").orElseThrow().getIdImdb());
    }

    @Test
    void createsActorAndPersistsCanonicalPersonWhenAbsent() {
        ActeurDTO dto = actorDto(personDto("John Doe", "01-01-1995"), "nm0000001");
        when(personStore.findByNconst("nm0000001")).thenReturn(Optional.empty());
        when(personStore.save(any(Personne.class))).thenAnswer(invocation -> {
            Personne person = invocation.getArgument(0);
            person.setId(1L);
            return person;
        });
        when(actorStore.save(any(Acteur.class))).thenAnswer(invocation -> {
            Acteur actor = invocation.getArgument(0);
            actor.setId(actor.getPersonne().getId());
            return actor;
        });

        ActeurDTO result = acteurService.createActeur(dto);

        assertEquals("John Doe", result.getPersonne().getIdentite());
        assertEquals("01-01-1995", result.getPersonne().getDateNaissance());
        assertEquals("1.80m", result.getTaille());
        assertEquals("nm0000001", result.getIdImdb());
        verify(personStore).save(any(Personne.class));
    }

    @Test
    void reusesExistingPersonWhenCreatingActorForExistingDirectorIdentity() {
        Personne existingPerson = new Personne("John Doe", "1995-01-01", "Paris", null);
        existingPerson.setId(7L);
        existingPerson.setNconst("nm0000001");
        when(personStore.findByNconst("nm0000001")).thenReturn(Optional.of(existingPerson));
        when(actorStore.save(any(Acteur.class))).thenAnswer(invocation -> {
            Acteur actor = invocation.getArgument(0);
            actor.setId(existingPerson.getId());
            return actor;
        });

        ActeurDTO result = acteurService.createActeur(actorDto(
                personDto("A different submitted name", "1995-01-01"), "nm0000001"));

        assertEquals("John Doe", result.getPersonne().getIdentite());
        ArgumentCaptor<Acteur> actorCaptor = ArgumentCaptor.forClass(Acteur.class);
        verify(actorStore).save(actorCaptor.capture());
        assertSame(existingPerson, actorCaptor.getValue().getPersonne());
        assertEquals(existingPerson.getId(), result.getId());
        verify(personStore, never()).save(any(Personne.class));
    }

    @Test
    void updatesExistingPersonWithoutReplacingSharedIdentity() {
        Personne person = new Personne("John Doe", "1990-01-01", "Paris", "https://example.com");
        person.setId(1L);
        person.setNconst("nm0000001");
        Acteur actor = new Acteur();
        actor.setId(1L);
        actor.setPersonne(person);
        when(actorStore.findById(1L)).thenReturn(Optional.of(actor));
        when(actorStore.save(actor)).thenReturn(actor);
        ActeurDTO dto = actorDto(personDto("Updated Name", "1991-02-03"), "nm0000001");
        dto.setTaille("1.80m");

        ActeurDTO result = acteurService.updateActeur(1L, dto);

        assertSame(person, actor.getPersonne());
        assertEquals("Updated Name", result.getPersonne().getIdentite());
        assertEquals("1991-02-03", result.getPersonne().getDateNaissance());
        verify(actorStore).save(actor);
    }

    @Test
    void rejectsInvalidActorData() {
        assertThrows(InvalidDataException.class, () -> acteurService.createActeur(new ActeurDTO()));
        verify(actorStore, never()).save(any(Acteur.class));
    }

    @Test
    void rejectsChangingAnExistingActorExternalIdentifier() {
        Personne person = new Personne("John Doe", "1990-01-01", "Paris", null);
        person.setId(1L);
        person.setNconst("nm0000001");
        Acteur actor = new Acteur();
        actor.setId(1L);
        actor.setPersonne(person);
        when(actorStore.findById(1L)).thenReturn(Optional.of(actor));
        ActeurDTO dto = actorDto(personDto("John Doe", "1990-01-01"), "nm0000002");

        assertThrows(InvalidDataException.class, () -> acteurService.updateActeur(1L, dto));
        verify(actorStore, never()).save(any(Acteur.class));
    }

    @Test
    void rejectsUpdateForMissingActor() {
        when(actorStore.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> acteurService.updateActeur(1L, new ActeurDTO()));
        verify(actorStore, never()).save(any(Acteur.class));
    }

    @Test
    void deletesExistingActor() {
        when(actorStore.existsById(1L)).thenReturn(true);
        acteurService.deleteActeur(1L);
        verify(actorStore).deleteById(1L);
    }

    @Test
    void rejectsDeletionForMissingActor() {
        when(actorStore.existsById(1L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> acteurService.deleteActeur(1L));
        verify(actorStore, never()).deleteById(anyLong());
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
