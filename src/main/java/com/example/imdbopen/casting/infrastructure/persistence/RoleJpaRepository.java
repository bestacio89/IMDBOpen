package com.example.imdbopen.casting.infrastructure.persistence;

import com.example.imdbopen.casting.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoleJpaRepository extends JpaRepository<Role, Long> {
    List<Role> findByRoleName(String roleName);

    @Query("SELECT r FROM Role r WHERE r.film.tconst = :filmId AND r.acteur.personne.nconst = :actorId")
    List<Role> findByFilmIdAndActorId(@Param("filmId") String filmId, @Param("actorId") String actorId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END FROM Role r "
            + "WHERE r.film.tconst = :filmId AND r.acteur.personne.nconst = :actorId AND r.roleName = :roleName")
    boolean existsByFilmIdAndActorIdAndRoleName(@Param("filmId") String filmId,
                                                @Param("actorId") String actorId,
                                                @Param("roleName") String roleName);
}
