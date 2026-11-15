package com.example.imdbopen.casting.application;

import com.example.imdbopen.actor.domain.Acteur;
import com.example.imdbopen.casting.application.port.out.ActorLookup;
import com.example.imdbopen.casting.application.port.out.FilmLookup;
import com.example.imdbopen.casting.application.port.out.RoleStore;
import com.example.imdbopen.casting.domain.Role;
import com.example.imdbopen.film.domain.Film;
import com.example.imdbopen.person.domain.Personne;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.imdbopen.casting.contracts.RoleDTO;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
    @Mock
    private RoleStore roleStore;
    @Mock
    private FilmLookup filmLookup;
    @Mock
    private ActorLookup actorLookup;
    @InjectMocks
    private RoleService roleService;

    @Test
    void createsCreditAfterResolvingExternalIdentifiers() {
        Film film = new Film();
        film.setImdb("tt0000001");
        Acteur actor = actor("nm0000001");
        when(filmLookup.findByImdb("tt0000001")).thenReturn(Optional.of(film));
        when(actorLookup.findByImdb("nm0000001")).thenReturn(Optional.of(actor));
        when(roleStore.save(any(Role.class))).thenAnswer(invocation -> {
            Role saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        RoleDTO dto = new RoleDTO();
        dto.setRoleName("Admin");
        dto.setFilmId("tt0000001");
        dto.setActeurId("nm0000001");

        RoleDTO result = roleService.createRole(dto);

        assertEquals("Admin", result.getRoleName());
        assertEquals("tt0000001", result.getFilmId());
        assertEquals("nm0000001", result.getActeurId());
        verify(roleStore).save(any(Role.class));
    }

    @Test
    void rejectsCreditWhenFilmCannotBeResolved() {
        when(filmLookup.findByImdb("tt-missing")).thenReturn(Optional.empty());
        RoleDTO dto = new RoleDTO();
        dto.setFilmId("tt-missing");
        dto.setActeurId("nm0000001");

        assertThrows(EntityNotFoundException.class, () -> roleService.createRole(dto));
        verify(roleStore, never()).save(any(Role.class));
        verifyNoInteractions(actorLookup);
    }

    @Test
    void rejectsCreditWhenActorCannotBeResolved() {
        Film film = new Film();
        film.setImdb("tt0000001");
        when(filmLookup.findByImdb("tt0000001")).thenReturn(Optional.of(film));
        when(actorLookup.findByImdb("nm-missing")).thenReturn(Optional.empty());
        RoleDTO dto = new RoleDTO();
        dto.setFilmId("tt0000001");
        dto.setActeurId("nm-missing");

        assertThrows(EntityNotFoundException.class, () -> roleService.createRole(dto));
        verify(roleStore, never()).save(any(Role.class));
    }

    @Test
    void rejectsCreditWithoutFilmIdentifier() {
        RoleDTO dto = new RoleDTO();
        dto.setActeurId("nm0000001");
        assertThrows(InvalidDataException.class, () -> roleService.createRole(dto));
        verifyNoInteractions(filmLookup, actorLookup, roleStore);
    }

    @Test
    void findsRolesByName() {
        when(roleStore.findByRoleName("Admin")).thenReturn(List.of(new Role("Admin", "tt1", "nm1")));
        assertEquals("Admin", roleService.findRolesByRoleName("Admin").get(0).getRoleName());
    }

    @Test
    void rejectsUnknownRoleName() {
        when(roleStore.findByRoleName("Missing")).thenReturn(List.of());
        assertThrows(EntityNotFoundException.class, () -> roleService.findRolesByRoleName("Missing"));
    }

    @Test
    void updatesRoleNameWithoutChangingAssociations() {
        Role role = new Role("Old", "tt0000001", "nm0000001");
        role.setId(1L);
        when(roleStore.findById(1L)).thenReturn(Optional.of(role));
        when(roleStore.save(any(Role.class))).thenReturn(role);
        RoleDTO dto = new RoleDTO();
        dto.setRoleName("User");

        RoleDTO result = roleService.updateRole(1L, dto);

        assertEquals("User", result.getRoleName());
        verify(roleStore).save(role);
        verifyNoInteractions(filmLookup, actorLookup);
    }

    @Test
    void deletesExistingRole() {
        Role role = new Role();
        role.setId(1L);
        when(roleStore.findById(1L)).thenReturn(Optional.of(role));

        roleService.deleteRole(1L);

        verify(roleStore).delete(role);
    }

    private Acteur actor(String imdbId) {
        Personne person = new Personne();
        person.setNconst(imdbId);
        Acteur actor = new Acteur();
        actor.setPersonne(person);
        return actor;
    }
}
