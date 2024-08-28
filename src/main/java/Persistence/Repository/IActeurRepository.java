package Persistence.Repository;

import Entities.Business.Personne.Acteur;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IActeurRepository extends IGenericRepository<Acteur, Long> {

    // Find by Nom and Prenom
    List<Acteur> findByNomAndPrenom(String nom, String prenom);

    // Find by Nom
    List<Acteur> findByNom(String nom);

    // Find by Prenom
    List<Acteur> findByPrenom(String prenom);

    // Find by IMDb ID
    Optional<Acteur> findByIdImdb(Long idImdb);

    // Find All by Role Name
    @Query("SELECT a FROM Acteur a JOIN a.roles r WHERE r.roleName = :roleName")
    List<Acteur> findAllByRoleName(@Param("roleName") String roleName);

    // Count Acteurs by Role Name
    @Query("SELECT COUNT(a) FROM Acteur a JOIN a.roles r WHERE r.roleName = :roleName")
    long countByRoleName(@Param("roleName") String roleName);

    // Find by Partial Match of Nom (Case-insensitive)
    List<Acteur> findByNomContainingIgnoreCase(String partialNom);

    // Find by Taille greater than a specified value
    List<Acteur> findByTailleGreaterThan(double taille);

    // Find by Taille smaller than a specified value
    List<Acteur> findByTailleLessThan(double taille);

    // Find by Date of Birth after a specific date
    List<Acteur> findByDateNaissanceAfter(LocalDateTime date);

    // Find by Date of Birth before a specific date
    List<Acteur> findByDateNaissanceBefore(LocalDateTime date);

    // Find by Date of Birth Range
    List<Acteur> findByDateNaissanceBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by CreatedDate before a specific date
    List<Acteur> findByCreatedDateBefore(LocalDateTime date);

    // Find by CreatedDate after a specific date
    List<Acteur> findByCreatedDateAfter(LocalDateTime date);

    // Find by CreatedDate within a specific range
    List<Acteur> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find All Acteurs Ordered by Nom (Alphabetical Order)
    List<Acteur> findAllByOrderByNomAsc();

    // Find All Acteurs Ordered by Nom (Reverse Alphabetical Order)
    List<Acteur> findAllByOrderByNomDesc();

    // Find by Nom or Prenom (Case-insensitive search for either field)
    List<Acteur> findByNomIgnoreCaseOrPrenomIgnoreCase(String nom, String prenom);

    // Find Top 3 Acteurs by Taille (e.g., tallest actors)
    List<Acteur> findTop3ByOrderByTailleDesc();

    // Find actors by age range
    List<Acteur> findActeurByTailleBetween(double minHeight, double maxHeight);

    // Find actors higher then
    List<Acteur> findActeurByTailleGreaterThan(double minHeight);

    // Find actors shorter then
    List<Acteur> findActeurByTailleLessThan(double minHeight);

}
