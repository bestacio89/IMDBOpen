package com.example.imdbopen.casting.application.port.out;

import com.example.imdbopen.casting.domain.Role;

import java.util.List;
import java.util.Optional;

public interface RoleStore {
    List<Role> findAll();
    List<Role> findByRoleName(String roleName);
    Optional<Role> findById(Long id);
    List<Role> findByFilmIdAndActorId(String filmId, String actorId);
    Role save(Role role);
    void delete(Role role);
}
