package com.example.imdbopen.casting.application;

import com.example.imdbopen.casting.application.port.out.RoleStore;
import com.example.imdbopen.casting.domain.Role;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.imdbopen.casting.contracts.CastingPrincipalDTO;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CastingPrincipalServiceTest {
    @Mock
    private RoleStore roleStore;
    private CastingPrincipalService service;

    @BeforeEach
    void setUp() {
        service = new CastingPrincipalService(roleStore);
    }

    @Test
    void retrievesPrincipalCredit() {
        Role credit = credit(true);
        when(roleStore.findByFilmIdAndActorId("film1", "acteur1")).thenReturn(List.of(credit));
        CastingPrincipalDTO result = service.getCastingByFilmAndActeur("film1", "acteur1");
        assertEquals("film1", result.getFilmId());
        assertEquals("acteur1", result.getActeurId());
    }

    @Test
    void marksExistingCreditAsPrincipal() {
        Role credit = credit(false);
        when(roleStore.findByFilmIdAndActorId("film1", "acteur1")).thenReturn(List.of(credit));
        service.createCasting(dto());
        assertTrue(credit.isPrincipal());
        verify(roleStore).save(credit);
    }

    @Test
    void rejectsAlreadyPrincipalCredit() {
        Role credit = credit(true);
        when(roleStore.findByFilmIdAndActorId("film1", "acteur1")).thenReturn(List.of(credit));
        assertThrows(InvalidDataException.class, () -> service.createCasting(dto()));
        verify(roleStore, never()).save(any());
    }

    @Test
    void rejectsMovingPrincipalFlagToAnotherCredit() {
        Role existing = credit(true);
        existing.setId(1L);
        Role target = credit(false);
        target.setId(2L);
        when(roleStore.findById(1L)).thenReturn(Optional.of(existing));
        when(roleStore.findByFilmIdAndActorId("film2", "acteur2")).thenReturn(List.of(target));
        assertThrows(InvalidDataException.class, () -> service.updateCasting(1L, dto("film2", "acteur2")));
        verify(roleStore, never()).save(any());
    }

    @Test
    void clearsPrincipalFlagWithoutDeletingUnderlyingCredit() {
        Role credit = credit(true);
        when(roleStore.findByFilmIdAndActorId("film1", "acteur1")).thenReturn(List.of(credit));
        service.deleteCastingByFilmAndActeur("film1", "acteur1");
        assertFalse(credit.isPrincipal());
        verify(roleStore).save(credit);
    }

    @Test
    void rejectsPrincipalCastingWithoutAnExistingCredit() {
        when(roleStore.findByFilmIdAndActorId("film1", "acteur1")).thenReturn(List.of());
        assertThrows(EntityNotFoundException.class, () -> service.createCasting(dto()));
    }

    @Test
    void rejectsMissingFilmOrActorIdentifiers() {
        assertThrows(InvalidDataException.class, () -> service.getCastingByFilmAndActeur("", "acteur1"));
        assertThrows(InvalidDataException.class, () -> service.createCasting(null));
        verifyNoInteractions(roleStore);
    }

    private Role credit(boolean principal) {
        Role role = new Role("Character", "film1", "acteur1");
        role.setPrincipal(principal);
        role.setId(1L);
        return role;
    }

    private CastingPrincipalDTO dto() {
        return dto("film1", "acteur1");
    }

    private CastingPrincipalDTO dto(String filmId, String actorId) {
        CastingPrincipalDTO dto = new CastingPrincipalDTO();
        dto.setFilmId(filmId);
        dto.setActeurId(actorId);
        return dto;
    }
}
