package com.example.imdbopen.genre.infrastructure.persistence;
import com.example.imdbopen.genre.domain.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface GenreJpaRepository extends JpaRepository<Genre, Long> {
    @Query("SELECT g FROM Genre g WHERE g.normalizedName = LOWER(TRIM(:name))")
    Optional<Genre> findByName(@Param("name") String name);
}
