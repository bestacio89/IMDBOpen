package persistence.repository;

import com.example.imdbopen.film.domain.FilmDirector;
import com.example.imdbopen.film.domain.FilmDirectorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFilmDirectorRepository extends JpaRepository<FilmDirector, FilmDirectorId> {
}
