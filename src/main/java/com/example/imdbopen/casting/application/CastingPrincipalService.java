package com.example.imdbopen.casting.application;

import com.example.imdbopen.casting.application.port.out.RoleStore;
import com.example.imdbopen.casting.domain.Role;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.model.dto.CastingPrincipalDTO;

import java.util.List;

@Service
public class CastingPrincipalService {
    private final RoleStore roleStore;

    public CastingPrincipalService(RoleStore roleStore) {
        this.roleStore = roleStore;
    }

    @Transactional(readOnly = true)
    public CastingPrincipalDTO getCastingByFilmAndActeur(String filmId, String acteurId) {
        validateIdentifiers(filmId, acteurId);
        Role credit = roleStore.findByFilmIdAndActorId(filmId, acteurId).stream()
                .filter(Role::isPrincipal)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Principal casting not found for the provided film and actor"));
        return toDto(credit);
    }

    @Transactional
    public void createCasting(CastingPrincipalDTO dto) {
        validateDto(dto);
        Role credit = findCredit(dto.getFilmId(), dto.getActeurId());
        if (credit.isPrincipal()) {
            throw new InvalidDataException("The film/actor credit is already principal cast");
        }
        credit.setPrincipal(true);
        roleStore.save(credit);
    }

    @Transactional
    public void updateCasting(Long id, CastingPrincipalDTO dto) {
        validateDto(dto);
        Role existing = roleStore.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Casting not found with id: " + id));
        Role target = findCredit(dto.getFilmId(), dto.getActeurId());
        if (!target.getId().equals(existing.getId())) {
            throw new InvalidDataException("A principal casting update cannot move a credit to another film/actor pair");
        }
        existing.setPrincipal(true);
        roleStore.save(existing);
    }

    @Transactional
    public void deleteCastingByFilmAndActeur(String filmId, String acteurId) {
        validateIdentifiers(filmId, acteurId);
        Role credit = roleStore.findByFilmIdAndActorId(filmId, acteurId).stream()
                .filter(Role::isPrincipal)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Principal casting not found for the provided film and actor"));
        credit.setPrincipal(false);
        roleStore.save(credit);
    }

    private Role findCredit(String filmId, String acteurId) {
        List<Role> credits = roleStore.findByFilmIdAndActorId(filmId, acteurId);
        return credits.stream().findFirst().orElseThrow(() -> new EntityNotFoundException(
                "The film/actor credit must exist before it can be marked as principal"));
    }

    private CastingPrincipalDTO toDto(Role role) {
        CastingPrincipalDTO dto = new CastingPrincipalDTO();
        dto.setIdCasting(role.getId());
        dto.setFilmId(role.getFilmId());
        dto.setActeurId(role.getActeurId());
        return dto;
    }

    private void validateDto(CastingPrincipalDTO dto) {
        if (dto == null) {
            throw new InvalidDataException("Casting data cannot be null");
        }
        validateIdentifiers(dto.getFilmId(), dto.getActeurId());
    }

    private void validateIdentifiers(String filmId, String acteurId) {
        if (filmId == null || filmId.isBlank() || acteurId == null || acteurId.isBlank()) {
            throw new InvalidDataException("Film ID and Actor ID are required");
        }
    }
}
