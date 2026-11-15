package entities.business.Film;

import entities.business.personne.Realisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "film_director")
@Getter
@Setter
@NoArgsConstructor
public class FilmDirector {
    @EmbeddedId
    private FilmDirectorId id;

    @MapsId("filmId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "film_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_film_director_film"))
    private Film film;

    @MapsId("directorId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "director_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_film_director_director"))
    private Realisateur director;

    public FilmDirector(Film film, Realisateur director) {
        this.film = film;
        this.director = director;
        this.id = new FilmDirectorId(film.getId(), director.getId());
    }
}
