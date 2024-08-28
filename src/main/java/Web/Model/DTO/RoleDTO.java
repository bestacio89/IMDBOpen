package Web.Model.DTO;

import Entities.Business.Role.Role;
import lombok.Data;

@Data
public class RoleDTO {
    private Long id;
    private String roleName;
    private FilmDTO film;
    private ActeurDTO actor;

    /**
     * Convert RoleDTO to Role entity.
     *
     * @return a Role entity
     */
    public Role toEntity() {
        Role role = new Role();
        role.setId(this.id);
        role.setRoleName(this.roleName);
        role.setFilm(this.film != null ? this.film.toEntity() : null);
        role.setActor(this.actor != null ? this.actor.toEntity() : null);
        return role;
    }

    /**
     * Convert Role entity to RoleDTO.
     *
     * @param role the Role entity to convert
     * @return a RoleDTO
     */
    public static RoleDTO fromEntity(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setRoleName(role.getRoleName());
        dto.setFilm(FilmDTO.fromEntity(role.getFilm()));
        dto.setActor(ActeurDTO.fromEntity(role.getActor()));
        return dto;
    }
}
