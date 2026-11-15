package com.example.imdbopen.director.domain;

import com.example.imdbopen.person.domain.Personne;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "director")
@Getter
@Setter
@NoArgsConstructor
public class Realisateur {
    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "person_id", nullable = false, foreignKey = @ForeignKey(name = "fk_director_person"))
    private Personne personne;

    public Realisateur(String identite, String dateNaissance, String lieuNaissance, String url, String idImdb) {
        this.personne = new Personne(identite, dateNaissance, lieuNaissance, url);
        this.personne.setNconst(idImdb);
    }

    public String getIdImdb() { return personne == null ? null : personne.getNconst(); }
    public void setIdImdb(String value) {
        if (personne == null) personne = new Personne();
        personne.setNconst(value);
    }
}
