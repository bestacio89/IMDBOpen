package service;

import com.example.imdbopen.actor.domain.Acteur;
import com.example.imdbopen.film.domain.Film;
import entities.business.role.Role;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import persistence.repository.IRoleRepository;
import persistence.repository.IFilmRepository;
import persistence.repository.IActeurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import web.model.dto.RoleDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {
    private final IRoleRepository roleRepository;
    private final IFilmRepository filmRepository;
    private final IActeurRepository actorRepository;

    public RoleService(IRoleRepository roleRepository, IFilmRepository filmRepository,
                       IActeurRepository actorRepository) {
        this.roleRepository = roleRepository;
        this.filmRepository = filmRepository;
        this.actorRepository = actorRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> findRolesWithFilters(String roleName, Long filmId, Long actorId) {
        List<Role> roles = roleRepository.findAll();
        if (StringUtils.hasText(roleName)) {
            roles = roles.stream()
                    .filter(role -> role.getRoleName() != null
                            && role.getRoleName().toLowerCase().contains(roleName.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (filmId != null) {
            roles = roles.stream().filter(role -> role.getFilm() != null
                    && filmId.equals(role.getFilm().getId())).collect(Collectors.toList());
        }
        if (actorId != null) {
            roles = roles.stream().filter(role -> role.getActeur() != null
                    && actorId.equals(role.getActeur().getId())).collect(Collectors.toList());
        }
        if (roles.isEmpty()) throw new EntityNotFoundException("No roles found matching the criteria");
        return roles.stream().map(RoleDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> findRolesByRoleName(String roleName) {
        List<Role> roles = roleRepository.findByRoleName(roleName);
        if (roles.isEmpty()) throw new EntityNotFoundException("No roles found with name: " + roleName);
        return roles.stream().map(RoleDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        if (roleDTO == null) throw new InvalidDataException("Role data cannot be null");
        Role role = roleDTO.toEntity();
        resolveAssociations(role);
        return RoleDTO.fromEntity(roleRepository.save(role));
    }

    @Transactional
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        if (roleDTO == null) throw new InvalidDataException("Role data cannot be null");
        Role existing = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + id));
        if (StringUtils.hasText(roleDTO.getRoleName())) existing.setRoleName(roleDTO.getRoleName());
        if (StringUtils.hasText(roleDTO.getFilmId())) existing.setFilm(resolveFilm(roleDTO.getFilmId()));
        if (StringUtils.hasText(roleDTO.getActeurId())) existing.setActeur(resolveActor(roleDTO.getActeurId()));
        return RoleDTO.fromEntity(roleRepository.save(existing));
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + id));
        roleRepository.delete(role);
    }

    private void resolveAssociations(Role role) {
        role.setFilm(resolveFilm(role.getFilmId()));
        role.setActeur(resolveActor(role.getActeurId()));
    }

    private Film resolveFilm(String tconst) {
        if (!StringUtils.hasText(tconst)) throw new InvalidDataException("Film IMDb identifier is required");
        return filmRepository.findByImdb(tconst)
                .orElseThrow(() -> new EntityNotFoundException("Film not found with IMDb identifier: " + tconst));
    }

    private Acteur resolveActor(String nconst) {
        if (!StringUtils.hasText(nconst)) throw new InvalidDataException("Actor IMDb identifier is required");
        return actorRepository.findByImdb(nconst)
                .orElseThrow(() -> new EntityNotFoundException("Actor not found with IMDb identifier: " + nconst));
    }
}
