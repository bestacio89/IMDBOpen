package entities.business.personne;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "actor")
@Getter
@Setter
@NoArgsConstructor
public class Acteur {
    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false, foreignKey = @ForeignKey(name = "fk_actor_person"))
    private Personne personne;

    @Column(name = "height_m", precision = 4, scale = 2)
    private BigDecimal heightMetres;

    @Column(name = "height_source", length = 32)
    private String heightSource;

    @Transient
    private String pendingImdbId;

    public Acteur(String idImdb, String taille) {
        this.pendingImdbId = idImdb;
        setTaille(taille);
    }

    public void setPersonne(Personne value) {
        this.personne = value;
        if (value != null && pendingImdbId != null) value.setNconst(pendingImdbId);
    }

    public String getIdImdb() {
        return personne == null ? pendingImdbId : personne.getNconst();
    }

    public void setIdImdb(String value) {
        this.pendingImdbId = value;
        if (personne != null) personne.setNconst(value);
    }

    public String getTaille() { return heightSource; }

    public void setTaille(String value) {
        this.heightSource = value;
        this.heightMetres = null;
        if (value != null && value.matches("\\d+(?:[.,]\\d+)?\\s*m?")) {
            this.heightMetres = new BigDecimal(value.replace("m", "").trim().replace(',', '.'));
        }
    }

    public void setPersonneDetails(String identite, String dateNaissance) {
        this.personne = new Personne();
        this.personne.setIdentite(identite);
        this.personne.setDateNaissance(dateNaissance);
        if (pendingImdbId != null) this.personne.setNconst(pendingImdbId);
    }
}
