package com.example.imdbopen.director.application;

import com.example.imdbopen.director.application.port.out.DirectorStore;
import com.example.imdbopen.director.domain.Realisateur;
import com.example.imdbopen.person.application.port.out.PersonStore;
import com.example.imdbopen.person.domain.Personne;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import exceptions.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.model.dto.RealisateurDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RealisateurService {
    private final DirectorStore directorStore;
    private final PersonStore personStore;

    public RealisateurService(DirectorStore directorStore, PersonStore personStore) {
        this.directorStore = directorStore;
        this.personStore = personStore;
    }

    public List<RealisateurDTO> findAll() {
        return directorStore.findAll().stream().map(RealisateurDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<RealisateurDTO> findById(Long id) {
        return directorStore.findById(id).map(RealisateurDTO::fromEntity);
    }

    public Optional<RealisateurDTO> findByIdImdb(String idImdb) {
        return directorStore.findByIdImdb(idImdb).map(RealisateurDTO::fromEntity);
    }

    @Transactional
    public RealisateurDTO save(RealisateurDTO dto) {
        if (dto == null || dto.getIdImdb() == null || dto.getIdImdb().isBlank()) {
            throw new InvalidDataException("Director IMDb identifier is required");
        }
        try {
            Personne person = personStore.findByNconst(dto.getIdImdb())
                    .orElseGet(() -> dto.toEntity().getPersonne());
            Realisateur director = new Realisateur();
            director.setPersonne(person);
            return RealisateurDTO.fromEntity(directorStore.save(director));
        } catch (InvalidDataException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ServiceException("Failed to save Realisateur: " + exception.getMessage());
        }
    }

    @Transactional
    public RealisateurDTO update(RealisateurDTO dto) {
        Realisateur existing = directorStore.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Realisateur not found with ID: " + dto.getId()));
        Personne person = existing.getPersonne();
        if (dto.getIdImdb() != null && !dto.getIdImdb().isBlank()
                && !dto.getIdImdb().equals(person.getNconst())) {
            throw new InvalidDataException("IMDb identifier cannot be changed for an existing director");
        }
        person.setIdentite(dto.getIdentite());
        person.setDateNaissance(dto.getDateNaissance());
        person.setLieuNaissance(dto.getLieuNaissance());
        person.setUrl(dto.getUrl());
        try {
            return RealisateurDTO.fromEntity(directorStore.save(existing));
        } catch (Exception exception) {
            throw new ServiceException("Failed to update Realisateur: " + exception.getMessage());
        }
    }

    @Transactional
    public void deleteById(Long id) {
        if (!directorStore.existsById(id)) {
            throw new EntityNotFoundException("Realisateur not found with ID: " + id);
        }
        directorStore.deleteById(id);
    }
}
