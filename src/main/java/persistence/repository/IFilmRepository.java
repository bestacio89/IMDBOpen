package persistence.repository;

import entities.business.Film.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface IFilmRepository extends JpaRepository<Film, Long> {
    @Query("SELECT f FROM Film f WHERE f.tconst = :imdb")
    Optional<Film> findByImdb(@Param("imdb") String imdb);

    @Query("SELECT f FROM Film f WHERE LOWER(f.title) LIKE LOWER(CONCAT('%', :nom, '%'))")
    List<Film> findByNomContainingIgnoreCase(@Param("nom") String nom);

    @Query("SELECT f FROM Film f WHERE f.releaseYearSource = :annee")
    List<Film> findByAnnee(@Param("annee") String annee);

    @Query("SELECT f FROM Film f WHERE f.ratingValue >= :rating")
    List<Film> findByRatingGreaterThanEqual(@Param("rating") BigDecimal rating);

    @Query("SELECT f FROM Film f WHERE f.country.name = :paysName")
    List<Film> findByPaysName(@Param("paysName") String paysName);

    @Query("SELECT f FROM Film f JOIN f.genresl g WHERE g.name = :genreName")
    List<Film> findByGenreName(@Param("genreName") String genreName);

    @Query("SELECT f FROM Film f WHERE LOWER(f.language) LIKE LOWER(CONCAT('%', :langue, '%'))")
    List<Film> findByLangueContainingIgnoreCase(@Param("langue") String langue);

    @Query("SELECT f FROM Film f WHERE LOWER(f.filmingLocations) LIKE LOWER(CONCAT('%', :lieuTour, '%'))")
    List<Film> findByLieuTourContainingIgnoreCase(@Param("lieuTour") String lieuTour);

    @Query("""
            SELECT DISTINCT f FROM Film f
            LEFT JOIN f.country c
            LEFT JOIN f.genresl g
            WHERE (:nom IS NULL OR LOWER(f.title) LIKE LOWER(CONCAT('%', :nom, '%')))
              AND (:annee IS NULL OR f.releaseYearSource = :annee)
              AND (:rating IS NULL OR f.ratingValue >= :ratingValue)
              AND (:paysName IS NULL OR c.name = :paysName)
              AND (:genreName IS NULL OR g.name = :genreName)
            ORDER BY f.title
            """)
    List<Film> findFilmsWithFiltersAndSorting(
            @Param("nom") String nom,
            @Param("annee") String annee,
            @Param("rating") String rating,
            @Param("paysName") String paysName,
            @Param("genreName") String genreName,
            @Param("sortBy") String sortBy,
            @Param("ratingValue") BigDecimal ratingValue
    );
}
