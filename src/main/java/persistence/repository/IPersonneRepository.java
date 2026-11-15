package persistence.repository;

import com.example.imdbopen.person.domain.Personne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPersonneRepository extends JpaRepository<Personne, Long> {
    Optional<Personne> findByNconst(String nconst);

    @Query("SELECT p FROM Personne p WHERE p.name = :nom")
    Optional<Personne> findByIdentite(@Param("nom") String nom);

    @Query("SELECT p FROM Personne p ORDER BY p.name ASC")
    List<Personne> findAllByOrderByIdentiteAsc();

    @Query("SELECT p FROM Personne p ORDER BY p.name DESC")
    List<Personne> findAllByOrderByIdentiteDesc();

    @Query("SELECT p FROM Personne p WHERE p.name = :identite AND p.birthDateSource = :dateNaissance")
    Optional<Personne> findByIdentiteAndDateNaissance(
            @Param("identite") String identite,
            @Param("dateNaissance") String dateNaissance);
}
