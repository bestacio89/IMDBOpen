package service;

import entities.business.personne.Personne;
import entities.business.personne.Realisateur;
import persistence.repository.IPersonneRepository;
import persistence.repository.IRealisateurRepository;
import web.model.dto.RealisateurDTO;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import exceptions.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RealisateurService {
    private final IRealisateurRepository realisateurRepository;
    private final IPersonneRepository personneRepository;

    public RealisateurService(IRealisateurRepository realisateurRepository,
                              IPersonneRepository personneRepository) {
        this.realisateurRepository = realisateurRepository;
        this.personneRepository = personneRepository;
    }

    public List<RealisateurDTO> findAll() {
        return realisateurRepository.findAll().stream().map(RealisateurDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<RealisateurDTO> findById(Long id) {
        return realisateurRepository.findById(id).map(RealisateurDTO::fromEntity);
    }

    public Optional<RealisateurDTO> findByIdImdb(String idImdb) {
        return realisateurRepository.findByIdImdb(idImdb).map(RealisateurDTO::fromEntity);
    }

    @Transactional
    public RealisateurDTO save(RealisateurDTO dto) {
        if (dto == null || dto.getIdImdb() == null || dto.getIdImdb().isBlank()) {
            throw new InvalidDataException("Director IMDb identifier is required");
        }
        try {
            Personne person = personneRepository.findByNconst(dto.getIdImdb())
                    .orElseGet(() -> dto.toEntity().getPersonne());
            Realisateur director = new Realisateur();
            director.setPersonne(person);
            return RealisateurDTO.fromEntity(realisateurRepository.save(director));
        } catch (InvalidDataException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ServiceException("Failed to save Realisateur: " + exception.getMessage());
        }
    }

    @Transactional
    public RealisateurDTO update(RealisateurDTO dto) {
        Realisateur existing = realisateurRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Realisateur not found with ID: " + dto.getId()));
        Personne person = existing.getPersonne();
        person.setIdentite(dto.getIdentite());
        person.setDateNaissance(dto.getDateNaissance());
        person.setLieuNaissance(dto.getLieuNaissance());
        person.setUrl(dto.getUrl());
        if (dto.getIdImdb() != null && !dto.getIdImdb().isBlank()
                && !dto.getIdImdb().equals(person.getNconst())) {
            throw new InvalidDataException("IMDb identifier cannot be changed for an existing director");
        }
        try {
            return RealisateurDTO.fromEntity(realisateurRepository.save(existing));
        } catch (Exception exception) {
            throw new ServiceException("Failed to update Realisateur: " + exception.getMessage());
        }
    }

    @Transactional
    public void deleteById(Long id) {
        if (!realisateurRepository.existsById(id)) {
            throw new EntityNotFoundException("Realisateur not found with ID: " + id);
        }
        realisateurRepository.deleteById(id);
    }
}
