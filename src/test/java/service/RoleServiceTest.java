package service;

import com.example.imdbopen.film.domain.Film;
import entities.business.personne.Acteur;
import com.example.imdbopen.person.domain.Personne;
import entities.business.role.Role;
import persistence.repository.IActeurRepository;
import persistence.repository.IFilmRepository;
import persistence.repository.IRoleRepository;
import web.model.dto.RoleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceTest {
    @InjectMocks
    private RoleService roleService;

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private IFilmRepository filmRepository;

    @Mock
    private IActeurRepository actorRepository;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void createsCreditAfterResolvingExternalIdentifiers() {
        Film film = new Film();
        film.setImdb("tt0000001");
        Acteur actor = new Acteur();
        Personne person = new Personne();
        person.setNconst("nm0000001");
        actor.setPersonne(person);

        when(filmRepository.findByImdb("tt0000001")).thenReturn(Optional.of(film));
        when(actorRepository.findByImdb("nm0000001")).thenReturn(Optional.of(actor));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
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
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void rejectsCreditWhenFilmCannotBeResolved() {
        when(filmRepository.findByImdb("tt-missing")).thenReturn(Optional.empty());
        RoleDTO dto = new RoleDTO();
        dto.setFilmId("tt-missing");
        dto.setActeurId("nm0000001");

        assertThrows(exceptions.EntityNotFoundException.class, () -> roleService.createRole(dto));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void updatesRoleNameWithoutChangingAssociations() {
        Role role = new Role("Old", "tt0000001", "nm0000001");
        role.setId(1L);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        RoleDTO dto = new RoleDTO();
        dto.setRoleName("User");

        RoleDTO result = roleService.updateRole(1L, dto);

        assertEquals("User", result.getRoleName());
        verify(roleRepository).save(role);
    }

    @Test
    void deletesExistingRole() {
        Role role = new Role();
        role.setId(1L);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        roleService.deleteRole(1L);

        verify(roleRepository).delete(role);
    }
}
