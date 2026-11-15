package com.example.imdbopen.casting.contracts;

import java.time.LocalDateTime;

public class CastingPrincipalDTO {
    private Long idCasting;
    private String filmId;
    private String acteurId;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public Long getIdCasting() {
        return idCasting;
    }

    public void setIdCasting(Long idCasting) {
        this.idCasting = idCasting;
    }

    public String getFilmId() {
        return filmId;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }

    public String getActeurId() {
        return acteurId;
    }

    public void setActeurId(String acteurId) {
        this.acteurId = acteurId;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
}
