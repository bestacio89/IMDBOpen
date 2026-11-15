package web.model.dto;

import entities.business.personne.Acteur;
import entities.business.personne.BirthDateParser;
import lombok.Data;

import java.util.List;

@Data
public class ActeurDTO {
    private Long id;
    private String idImdb;
    private String taille;
    private PersonneDTO personne;
    private String dateNaissance;
    private String lieuNaissance;
    private String url;
    private List<FilmDTO> films;
    private List<RoleDTO> roles;

    public static ActeurDTO fromEntity(Acteur acteur) {
        ActeurDTO dto = new ActeurDTO();
        dto.setId(acteur.getId());
        dto.setIdImdb(acteur.getIdImdb());
        dto.setTaille(acteur.getTaille());
        dto.setPersonne(PersonneDTO.fromEntity(acteur.getPersonne()));
        dto.setDateNaissance(acteur.getPersonne().getDateNaissance());
        dto.setLieuNaissance(acteur.getPersonne().getLieuNaissance());
        dto.setUrl(acteur.getPersonne().getUrl());
        return dto;
    }

    public Acteur toEntity() {
        Acteur acteur = new Acteur();
        acteur.setIdImdb(this.idImdb);
        acteur.setTaille(this.taille);
        return acteur;
    }

    public boolean isValidDateNaissance() {
        return personne != null && BirthDateParser.parse(personne.getDateNaissance()).isPresent();
    }
}
