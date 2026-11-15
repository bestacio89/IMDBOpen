package service;

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
        Realisateur updated = dto.toEntity();
        updated.setId(existing.getId());
        try {
            return RealisateurDTO.fromEntity(realisateurRepository.save(updated));
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
