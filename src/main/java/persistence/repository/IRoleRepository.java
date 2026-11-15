package persistence.repository;

import com.example.imdbopen.casting.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {
    @Query("SELECT r FROM Role r WHERE r.film.tconst = :filmId")
    List<Role> findByFilmId(@Param("filmId") String filmId);

    @Query("SELECT r FROM Role r WHERE r.acteur.personne.nconst = :actorId")
    List<Role> findByActorId(@Param("actorId") String actorId);

    List<Role> findByRoleName(String roleName);

    @Query("SELECT r FROM Role r WHERE r.film.tconst = :filmId AND r.roleName = :roleName")
    List<Role> findByFilmIdAndRoleName(@Param("filmId") String filmId, @Param("roleName") String roleName);

    @Query("SELECT r FROM Role r WHERE r.acteur.personne.nconst = :actorId AND r.roleName = :roleName")
    List<Role> findByActorIdAndRoleName(@Param("actorId") String actorId, @Param("roleName") String roleName);

    @Query("SELECT r FROM Role r WHERE r.film.tconst = :filmId AND r.acteur.personne.nconst = :actorId")
    List<Role> findByFilmIdAndActorId(@Param("filmId") String filmId, @Param("actorId") String actorId);

    @Query("SELECT r FROM Role r WHERE LOWER(r.roleName) LIKE LOWER(CONCAT('%', :partialRoleName, '%'))")
    List<Role> findByRoleNameContaining(@Param("partialRoleName") String partialRoleName);

    @Query("SELECT r FROM Role r WHERE r.film.tconst = :filmId AND r.acteur.personne.nconst = :acteurId AND r.roleName = :roleName")
    Optional<Role> findRoleByFilmIdAndActeurIdAndRoleName(
            @Param("filmId") String filmId,
            @Param("acteurId") String acteurId,
            @Param("roleName") String roleName);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END FROM Role r WHERE r.film.tconst = :filmId AND r.acteur.personne.nconst = :acteurId AND r.roleName = :roleName")
    boolean existsByFilmIdAndActeurIdAndRoleName(
            @Param("filmId") String filmId,
            @Param("acteurId") String acteurId,
            @Param("roleName") String roleName);

    Optional<Role> findFirstByFilm_TconstAndActeur_Personne_NconstAndPrincipalTrue(
            String filmId, String actorId);

    List<Role> findByFilm_TconstAndActeur_Personne_Nconst(String filmId, String actorId);
}
