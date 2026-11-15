package com.example.imdbopen.director.contracts;

import com.example.imdbopen.director.domain.Realisateur;
import com.example.imdbopen.person.domain.Personne;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class RealisateurDTO {
    private Long id;
    private String identite;
    private String dateNaissance;
    private String lieuNaissance;
    private String url;
    private String idImdb;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public RealisateurDTO(Long id, String identite, String dateNaissance,
                          String lieuNaissance, String url, String idImdb,
                          LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.identite = identite;
        this.dateNaissance = dateNaissance;
        this.lieuNaissance = lieuNaissance;
        this.url = url;
        this.idImdb = idImdb;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public static RealisateurDTO fromEntity(Realisateur realisateur) {
        if (realisateur == null || realisateur.getPersonne() == null) {
            return null;
        }
        Personne person = realisateur.getPersonne();
        return new RealisateurDTO(realisateur.getId(), person.getIdentite(), person.getDateNaissance(),
                person.getLieuNaissance(), person.getUrl(), realisateur.getIdImdb(), null, null);
    }

    public Realisateur toEntity() {
        Personne person = new Personne(identite, dateNaissance, lieuNaissance, url);
        person.setNconst(idImdb);
        Realisateur director = new Realisateur();
        director.setId(id);
        director.setPersonne(person);
        return director;
    }
}
