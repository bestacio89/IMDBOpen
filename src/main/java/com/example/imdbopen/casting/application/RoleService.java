package com.example.imdbopen.casting.application;

import com.example.imdbopen.actor.domain.Acteur;
import com.example.imdbopen.casting.application.port.out.ActorLookup;
import com.example.imdbopen.casting.application.port.out.FilmLookup;
import com.example.imdbopen.casting.application.port.out.RoleStore;
import com.example.imdbopen.casting.domain.Role;
import com.example.imdbopen.film.domain.Film;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.example.imdbopen.casting.contracts.RoleDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {
    private final RoleStore roleStore;
    private final FilmLookup filmLookup;
    private final ActorLookup actorLookup;

    public RoleService(RoleStore roleStore, FilmLookup filmLookup, ActorLookup actorLookup) {
        this.roleStore = roleStore;
        this.filmLookup = filmLookup;
        this.actorLookup = actorLookup;
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> findRolesWithFilters(String roleName, Long filmId, Long actorId) {
        List<Role> roles = roleStore.findAll();
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
        if (roles.isEmpty()) {
            throw new EntityNotFoundException("No roles found matching the criteria");
        }
        return roles.stream().map(RoleDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> findRolesByRoleName(String roleName) {
        List<Role> roles = roleStore.findByRoleName(roleName);
        if (roles.isEmpty()) {
            throw new EntityNotFoundException("No roles found with name: " + roleName);
        }
        return roles.stream().map(RoleDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public boolean createRoleIfAbsent(RoleDTO roleDTO) {
        if (roleDTO == null) {
            throw new InvalidDataException("Role data cannot be null");
        }
        if (!StringUtils.hasText(roleDTO.getFilmId()) || !StringUtils.hasText(roleDTO.getActeurId())
                || !StringUtils.hasText(roleDTO.getRoleName())) {
            throw new InvalidDataException("Film ID, actor ID and role name are required");
        }
        if (roleStore.existsByFilmIdAndActorIdAndRoleName(
                roleDTO.getFilmId(), roleDTO.getActeurId(), roleDTO.getRoleName())) {
            return false;
        }
        createRole(roleDTO);
        return true;
    }

    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        if (roleDTO == null) {
            throw new InvalidDataException("Role data cannot be null");
        }
        Role role = roleDTO.toEntity();
        resolveAssociations(role);
        return RoleDTO.fromEntity(roleStore.save(role));
    }

    @Transactional
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        if (roleDTO == null) {
            throw new InvalidDataException("Role data cannot be null");
        }
        Role existing = roleStore.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + id));
        if (StringUtils.hasText(roleDTO.getRoleName())) {
            existing.setRoleName(roleDTO.getRoleName());
        }
        if (StringUtils.hasText(roleDTO.getFilmId())) {
            existing.setFilm(resolveFilm(roleDTO.getFilmId()));
        }
        if (StringUtils.hasText(roleDTO.getActeurId())) {
            existing.setActeur(resolveActor(roleDTO.getActeurId()));
        }
        return RoleDTO.fromEntity(roleStore.save(existing));
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = roleStore.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + id));
        roleStore.delete(role);
    }

    private void resolveAssociations(Role role) {
        role.setFilm(resolveFilm(role.getFilmId()));
        role.setActeur(resolveActor(role.getActeurId()));
    }

    private Film resolveFilm(String tconst) {
        if (!StringUtils.hasText(tconst)) {
            throw new InvalidDataException("Film IMDb identifier is required");
        }
        return filmLookup.findByImdb(tconst)
                .orElseThrow(() -> new EntityNotFoundException("Film not found with IMDb identifier: " + tconst));
    }

    private Acteur resolveActor(String nconst) {
        if (!StringUtils.hasText(nconst)) {
            throw new InvalidDataException("Actor IMDb identifier is required");
        }
        return actorLookup.findByImdb(nconst)
                .orElseThrow(() -> new EntityNotFoundException("Actor not found with IMDb identifier: " + nconst));
    }
}
