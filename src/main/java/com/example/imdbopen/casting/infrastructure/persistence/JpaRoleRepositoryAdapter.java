package com.example.imdbopen.casting.infrastructure.persistence;

import com.example.imdbopen.casting.application.port.out.RoleStore;
import com.example.imdbopen.casting.domain.Role;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaRoleRepositoryAdapter implements RoleStore {
    private final RoleJpaRepository repository;

    public JpaRoleRepositoryAdapter(RoleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Role> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Role> findByRoleName(String roleName) {
        return repository.findByRoleName(roleName);
    }

    @Override
    public Optional<Role> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Role> findByFilmIdAndActorId(String filmId, String actorId) {
        return repository.findByFilmIdAndActorId(filmId, actorId);
    }

    @Override
    public Role save(Role role) {
        return repository.save(role);
    }

    @Override
    public void delete(Role role) {
        repository.delete(role);
    }
}
