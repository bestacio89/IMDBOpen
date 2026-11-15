package persistence.repository;

import com.example.imdbopen.genre.domain.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IGenreRepository extends JpaRepository<Genre, Long> {
    @Query("SELECT g FROM Genre g WHERE g.normalizedName = LOWER(TRIM(:name))")
    Optional<Genre> findByName(@Param("name") String name);
}
