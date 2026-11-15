package persistence.repository;

import entities.business.personne.Acteur;
import entities.business.personne.Personne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IActeurRepository extends JpaRepository<Acteur, Long> {
    @Query("SELECT a FROM Acteur a WHERE a.personne.nconst = :imdb")
    Optional<Acteur> findByImdb(@Param("imdb") String imdb);

    Optional<Acteur> findByPersonne(Personne personne);
}
