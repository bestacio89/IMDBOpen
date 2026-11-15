package entities.business.role;

import com.example.imdbopen.actor.domain.Acteur;
import com.example.imdbopen.film.domain.Film;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "film_cast", uniqueConstraints = @UniqueConstraint(name = "uk_film_cast_film_actor", columnNames = {"film_id", "actor_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "film_id", nullable = false, foreignKey = @ForeignKey(name = "fk_film_cast_film"))
    private Film film;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_film_cast_actor"))
    private Acteur acteur;

    @Column(name = "character_name", columnDefinition = "TEXT")
    private String roleName;

    @Column(name = "is_principal", nullable = false)
    private boolean principal;

    @Transient
    private String pendingFilmId;

    @Transient
    private String pendingActeurId;

    public Role(String roleName, String filmId, String acteurId) {
        this.roleName = roleName;
        this.pendingFilmId = filmId;
        this.pendingActeurId = acteurId;
    }

    public String getFilmId() { return film == null ? pendingFilmId : film.getImdb(); }
    public void setFilmId(String value) { this.pendingFilmId = value; }
    public String getActeurId() { return acteur == null ? pendingActeurId : acteur.getIdImdb(); }
    public void setActeurId(String value) { this.pendingActeurId = value; }

    @Override
    public String toString() {
        return "Role{id=" + id + ", roleName='" + roleName + "', filmId='" + getFilmId()
                + "', acteurId='" + getActeurId() + "'}";
    }
}
