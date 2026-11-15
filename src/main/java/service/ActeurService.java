package service;

import entities.business.personne.Acteur;
import entities.business.personne.Personne;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import web.model.dto.ActeurDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import persistence.repository.IActeurRepository;
import persistence.repository.IPersonneRepository;


@Service
public class ActeurService {
    private final IActeurRepository acteurRepository;
    private final IPersonneRepository personneRepository;

    public ActeurService(IActeurRepository acteurRepository, IPersonneRepository personneRepository) {
        this.acteurRepository = acteurRepository;
        this.personneRepository = personneRepository;
    }

    @Transactional
    public ActeurDTO createActeur(ActeurDTO dto) {
        if (dto == null || dto.getPersonne() == null) {
            throw new InvalidDataException("Invalid acteur or personne data");
        }
        if (!dto.isValidDateNaissance()) {
            throw new InvalidDataException("Invalid date of birth format");
        }
        if (dto.getIdImdb() == null || dto.getIdImdb().isBlank()) {
            throw new InvalidDataException("Actor IMDb identifier is required");
        }

        Personne person = personneRepository.findByNconst(dto.getIdImdb())
                .orElseGet(() -> {
                    Personne candidate = dto.getPersonne().toEntity();
                    candidate.setNconst(dto.getIdImdb());
                    return personneRepository.save(candidate);
                });

        Acteur actor = dto.toEntity();
        actor.setPersonne(person);
        Acteur saved = acteurRepository.save(actor);
        return ActeurDTO.fromEntity(saved);
    }

    @Transactional
    public ActeurDTO updateActeur(Long id, ActeurDTO dto) {
        if (dto == null) throw new InvalidDataException("Actor data cannot be null");
        Acteur existing = acteurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Acteur not found with id: " + id));

        if (dto.getPersonne() != null && !dto.isValidDateNaissance()) {
            throw new InvalidDataException("Invalid date of birth format");
        }

        if (dto.getIdImdb() != null && !dto.getIdImdb().isBlank()
                && !dto.getIdImdb().equals(existing.getIdImdb())) {
            throw new InvalidDataException("IMDb identifier cannot be changed for an existing actor");
        }
        if (dto.getTaille() != null) existing.setTaille(dto.getTaille());

        if (dto.getPersonne() != null) {
            Personne person = existing.getPersonne();
            person.setIdentite(dto.getPersonne().getIdentite());
            person.setDateNaissance(dto.getPersonne().getDateNaissance());
            person.setLieuNaissance(dto.getPersonne().getLieuNaissance());
            person.setUrl(dto.getPersonne().getUrl());
        }

        return ActeurDTO.fromEntity(acteurRepository.save(existing));
    }

    @Transactional
    public void deleteActeur(Long id) {
        if (!acteurRepository.existsById(id)) {
            throw new EntityNotFoundException("Acteur not found with id: " + id);
        }
        acteurRepository.deleteById(id);
    }
}
