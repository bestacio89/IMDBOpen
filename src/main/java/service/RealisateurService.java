package service;

import entities.business.personne.Personne;
import entities.business.personne.Realisateur;
import persistence.repository.IRealisateurRepository;
import web.model.dto.RealisateurDTO;
import exceptions.EntityNotFoundException;
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

    public RealisateurService(IRealisateurRepository realisateurRepository) {
        this.realisateurRepository = realisateurRepository;
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
        try {
            return RealisateurDTO.fromEntity(realisateurRepository.save(dto.toEntity()));
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
        person.setNconst(dto.getIdImdb());
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
