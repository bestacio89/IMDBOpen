package Web.Model.DTO;

import Entities.Business.Personne.Acteur;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for the Acteur entity.
 * <p>
 * This class is used to transfer data between different layers of the application
 * without exposing the full entity.
 * </p>
 *
 * @author filipdabrowski
 */
@Data
public class ActeurDTO {

    private long idActeur;
    private long idImdb;
    private String nom;
    private String prenom;
    private LocalDateTime dateNaissance;
    private double taille;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    /**
     * Converts an Acteur entity to an ActeurDTO.
     *
     * @param acteur the Acteur entity to be converted
     * @return the corresponding ActeurDTO
     */
    public static ActeurDTO toDto(Acteur acteur) {
        // Create a new DTO object
        ActeurDTO acteurDTO = new ActeurDTO();

        // Map the entity fields to the DTO fields
        acteurDTO.setIdActeur(acteur.getIdActeur());
        acteurDTO.setIdImdb(acteur.getIdImdb());
        acteurDTO.setNom(acteur.getNom());
        acteurDTO.setPrenom(acteur.getPrenom());
        acteurDTO.setDateNaissance(acteur.getDateNaissance());
        acteurDTO.setTaille(acteur.getTaille());
        acteurDTO.setCreatedDate(acteur.getCreatedDate());
        acteurDTO.setUpdatedDate(acteur.getUpdatedDate());

        // Return the constructed DTO
        return acteurDTO;
    }

    /**
     * Converts an ActeurDTO back to an Acteur entity.
     *
     * @param dto the ActeurDTO to be converted
     * @return the corresponding Acteur entity
     */
    public static Acteur toEntity(ActeurDTO dto) {
        // Create a new Acteur entity object
        Acteur acteur = new Acteur();

        // Map the DTO fields to the entity fields
        acteur.setIdActeur(dto.getIdActeur());
        acteur.setIdImdb(dto.getIdImdb());
        acteur.setNom(dto.getNom());
        acteur.setPrenom(dto.getPrenom());
        acteur.setDateNaissance(dto.getDateNaissance());
        acteur.setTaille(dto.getTaille());
        acteur.setCreatedDate(dto.getCreatedDate());
        acteur.setUpdatedDate(dto.getUpdatedDate());

        // Return the constructed entity
        return acteur;
    }

}
