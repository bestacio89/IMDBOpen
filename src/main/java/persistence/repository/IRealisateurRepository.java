package persistence.repository;

import com.example.imdbopen.person.domain.Personne;
import entities.business.personne.Realisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRealisateurRepository extends JpaRepository<Realisateur, Long> {
    @Query("SELECT d FROM Realisateur d WHERE d.personne.nconst = :idImdb")
    Optional<Realisateur> findByIdImdb(@Param("idImdb") String idImdb);

    Optional<Realisateur> findByPersonne(Personne personne);
}
