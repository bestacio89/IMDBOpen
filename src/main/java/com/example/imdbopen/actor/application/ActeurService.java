package com.example.imdbopen.actor.application;

import com.example.imdbopen.actor.application.port.out.ActorStore;
import com.example.imdbopen.actor.domain.Acteur;
import com.example.imdbopen.person.application.port.out.PersonStore;
import com.example.imdbopen.person.domain.Personne;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.imdbopen.actor.contracts.ActeurDTO;

@Service
public class ActeurService {
    private final ActorStore actorStore;
    private final PersonStore personStore;

    public ActeurService(ActorStore actorStore, PersonStore personStore) {
        this.actorStore = actorStore;
        this.personStore = personStore;
    }

    @Transactional
    public ActeurDTO createActeur(ActeurDTO dto) {
        if (dto == null || dto.getPersonne() == null) throw new InvalidDataException("Invalid acteur or personne data");
        if (!dto.isValidDateNaissance()) throw new InvalidDataException("Invalid date of birth format");
        if (dto.getIdImdb() == null || dto.getIdImdb().isBlank()) throw new InvalidDataException("Actor IMDb identifier is required");
        Personne person = personStore.findByNconst(dto.getIdImdb()).orElseGet(() -> {
            Personne candidate = dto.getPersonne().toEntity();
            candidate.setNconst(dto.getIdImdb());
            return personStore.save(candidate);
        });
        Acteur actor = dto.toEntity();
        actor.setPersonne(person);
        return ActeurDTO.fromEntity(actorStore.save(actor));
    }

    @Transactional
    public ActeurDTO updateActeur(Long id, ActeurDTO dto) {
        if (dto == null) throw new InvalidDataException("Actor data cannot be null");
        Acteur existing = actorStore.findById(id).orElseThrow(() -> new EntityNotFoundException("Acteur not found with id: " + id));
        if (dto.getPersonne() != null && !dto.isValidDateNaissance()) throw new InvalidDataException("Invalid date of birth format");
        if (dto.getIdImdb() != null && !dto.getIdImdb().isBlank() && !dto.getIdImdb().equals(existing.getIdImdb()))
            throw new InvalidDataException("IMDb identifier cannot be changed for an existing actor");
        if (dto.getTaille() != null) existing.setTaille(dto.getTaille());
        if (dto.getPersonne() != null) {
            Personne person = existing.getPersonne();
            person.setIdentite(dto.getPersonne().getIdentite());
            person.setDateNaissance(dto.getPersonne().getDateNaissance());
            person.setLieuNaissance(dto.getPersonne().getLieuNaissance());
            person.setUrl(dto.getPersonne().getUrl());
        }
        return ActeurDTO.fromEntity(actorStore.save(existing));
    }

    @Transactional
    public void deleteActeur(Long id) {
        if (!actorStore.existsById(id)) throw new EntityNotFoundException("Acteur not found with id: " + id);
        actorStore.deleteById(id);
    }
}
