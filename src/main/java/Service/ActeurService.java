package Service;

import Entities.Business.Personne.Acteur;
import Persistence.Repository.IActeurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ActeurService {

    private final IActeurRepository acteurRepository;

    @Autowired
    public ActeurService(IActeurRepository acteurRepository) {
        this.acteurRepository = acteurRepository;
    }

    // Get by Full Name (Last Name and First Name)
    public List<Acteur> getByFullName(String lastName, String firstName) {
        return acteurRepository.findByNomAndPrenom(lastName, firstName);
    }

    // Get by Last Name
    public List<Acteur> getByLastName(String lastName) {
        return acteurRepository.findByNom(lastName);
    }

    // Get by First Name
    public List<Acteur> getByFirstName(String firstName) {
        return acteurRepository.findByPrenom(firstName);
    }

    // Get by IMDb ID
    public Optional<Acteur> getByImdbId(Long imdbId) {
        return acteurRepository.findByIdImdb(imdbId);
    }

    // Get All by Role Name
    public List<Acteur> getByRoleName(String roleName) {
        return acteurRepository.findAllByRoleName(roleName);
    }

    // Count by Role Name
    public long countByRoleName(String roleName) {
        return acteurRepository.countByRoleName(roleName);
    }

    // Search by Partial Last Name
    public List<Acteur> searchByLastName(String partialLastName) {
        return acteurRepository.findByNomContainingIgnoreCase(partialLastName);
    }

    // Get Actors Taller Than a Specific Height
    public List<Acteur> getTallerThan(double height) {
        return acteurRepository.findByTailleGreaterThan(height);
    }

    // Get Actors Shorter Than a Specific Height
    public List<Acteur> getShorterThan(double height) {
        return acteurRepository.findByTailleLessThan(height);
    }

    // Get Actors Born After a Specific Date
    public List<Acteur> getBornAfter(LocalDateTime date) {
        return acteurRepository.findByDateNaissanceAfter(date);
    }

    // Get Actors Born Before a Specific Date
    public List<Acteur> getBornBefore(LocalDateTime date) {
        return acteurRepository.findByDateNaissanceBefore(date);
    }

    // Get Actors Born Between Specific Dates
    public List<Acteur> getBornBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return acteurRepository.findByDateNaissanceBetween(startDate, endDate);
    }

    // Get Actors Created Before a Specific Date
    public List<Acteur> getCreatedBefore(LocalDateTime date) {
        return acteurRepository.findByCreatedDateBefore(date);
    }

    // Get Actors Created After a Specific Date
    public List<Acteur> getCreatedAfter(LocalDateTime date) {
        return acteurRepository.findByCreatedDateAfter(date);
    }

    // Get Actors Created Between Specific Dates
    public List<Acteur> getCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return acteurRepository.findByCreatedDateBetween(startDate, endDate);
    }

    // Get All Actors Ordered by Last Name Ascending
    public List<Acteur> getAllOrderedByLastNameAsc() {
        return acteurRepository.findAllByOrderByNomAsc();
    }

    // Get All Actors Ordered by Last Name Descending
    public List<Acteur> getAllOrderedByLastNameDesc() {
        return acteurRepository.findAllByOrderByNomDesc();
    }

    // Search by First or Last Name (Case-insensitive)
    public List<Acteur> searchByFirstOrLastName(String lastName, String firstName) {
        return acteurRepository.findByNomIgnoreCaseOrPrenomIgnoreCase(lastName, firstName);
    }

    // Get Top 3 Tallest Actors
    public List<Acteur> getTop3Tallest() {
        return acteurRepository.findTop3ByOrderByTailleDesc();
    }

    // Get Actors by Height Range
    public List<Acteur> getByHeightRange(double minHeight, double maxHeight) {
        return acteurRepository.findActeurByTailleBetween(minHeight, maxHeight);
    }

    /* todo after we have the remaining classes we can add:
        top N by Rating
        actors by Country
        actors by City
        actors with most collaborations with specific directors
        actors by age range
        most frequent actor collaborations in films
        actors by genre specialization
     */

}
