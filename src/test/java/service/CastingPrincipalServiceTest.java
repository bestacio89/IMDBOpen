package service;

import com.example.imdbopen.casting.domain.Role;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import persistence.repository.IRoleRepository;
import web.model.dto.CastingPrincipalDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CastingPrincipalServiceTest {
    @Mock
    private IRoleRepository roleRepository;

    private CastingPrincipalService service;

    @BeforeEach
    void setUp() {
        service = new CastingPrincipalService(roleRepository);
    }

    @Test
    void retrievesPrincipalCredit() {
        Role credit = credit(true);
        when(roleRepository.findByFilmIdAndActorId("film1", "acteur1")).thenReturn(List.of(credit));

        CastingPrincipalDTO result = service.getCastingByFilmAndActeur("film1", "acteur1");

        assertEquals("film1", result.getFilmId());
        assertEquals("acteur1", result.getActeurId());
    }

    @Test
    void marksExistingCreditAsPrincipal() {
        Role credit = credit(false);
        when(roleRepository.findByFilmIdAndActorId("film1", "acteur1")).thenReturn(List.of(credit));

        service.createCasting(dto());

        assertTrue(credit.isPrincipal());
        verify(roleRepository).save(credit);
    }

    @Test
    void rejectsMovingPrincipalFlagToAnotherCredit() {
        Role existing = credit(true);
        existing.setId(1L);
        Role target = credit(false);
        target.setId(2L);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roleRepository.findByFilmIdAndActorId("film2", "acteur2")).thenReturn(List.of(target));

        assertThrows(InvalidDataException.class, () -> service.updateCasting(1L, dto("film2", "acteur2")));
        verify(roleRepository, never()).save(any());
    }

    @Test
    void clearsPrincipalFlagWithoutDeletingTheUnderlyingCredit() {
        Role credit = credit(true);
        when(roleRepository.findByFilmIdAndActorId("film1", "acteur1")).thenReturn(List.of(credit));

        service.deleteCastingByFilmAndActeur("film1", "acteur1");

        assertFalse(credit.isPrincipal());
        verify(roleRepository).save(credit);
        verify(roleRepository, never()).delete(any());
    }

    @Test
    void rejectsPrincipalCastingWithoutAnExistingCredit() {
        when(roleRepository.findByFilmIdAndActorId("film1", "acteur1")).thenReturn(List.of());
        assertThrows(EntityNotFoundException.class, () -> service.createCasting(dto()));
    }

    private Role credit(boolean principal) {
        Role role = new Role("Character", "film1", "acteur1");
        role.setPrincipal(principal);
        return role;
    }

    private CastingPrincipalDTO dto() { return dto("film1", "acteur1"); }

    private CastingPrincipalDTO dto(String filmId, String actorId) {
        CastingPrincipalDTO dto = new CastingPrincipalDTO();
        dto.setFilmId(filmId);
        dto.setActeurId(actorId);
        return dto;
    }
}
