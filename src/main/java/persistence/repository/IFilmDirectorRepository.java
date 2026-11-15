package persistence.repository;

import entities.business.Film.FilmDirector;
import entities.business.Film.FilmDirectorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFilmDirectorRepository extends JpaRepository<FilmDirector, FilmDirectorId> {
}
