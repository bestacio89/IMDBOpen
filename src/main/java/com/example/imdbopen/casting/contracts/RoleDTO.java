package com.example.imdbopen.casting.contracts;

import com.example.imdbopen.casting.domain.Role;
import lombok.Getter;
import lombok.Setter;

public class RoleDTO {
    @Setter
    @Getter
    private Long id;
    @Setter
    @Getter
    private String roleName;
    private String filmId;
    private String acteurId;

    public RoleDTO() {
    }

    public RoleDTO(Long id, String roleName, String filmId, String acteurId) {
        this.id = id;
        this.roleName = roleName;
        this.filmId = filmId;
        this.acteurId = acteurId;
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

    public static RoleDTO fromEntity(Role role) {
        return new RoleDTO(role.getId(), role.getRoleName(), role.getFilmId(), role.getActeurId());
    }

    public Role toEntity() {
        Role role = new Role();
        role.setId(this.id);
        role.setRoleName(this.roleName);
        role.setFilmId(this.filmId);
        role.setActeurId(this.acteurId);
        return role;
    }
}
