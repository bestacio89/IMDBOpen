package Web.Controller;

import Exceptions.EntityNotFoundException;
import Exceptions.InvalidDataException;
import Service.RoleService;
import Web.Model.DTO.RoleDTO;
import Web.Model.Generic.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> searchRoles(
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Long filmId,
            @RequestParam(required = false) Long actorId
    ) {
        try {
            List<RoleDTO> roles = roleService.findRolesWithFilters(roleName, filmId, actorId);
            ApiResponse<List<RoleDTO>> response = new ApiResponse<>(HttpStatus.OK.value(), "Roles retrieved successfully", roles);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (InvalidDataException ex) {
            throw ex;  // Handled by the Global Exception Handler
        } catch (Exception ex) {
            throw new RuntimeException("An error occurred while searching for roles", ex);  // Handled by the Global Exception Handler
        }
    }

    @GetMapping("/name/{roleName}")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getRolesByName(@PathVariable String roleName) {
        try {
            List<RoleDTO> roles = roleService.findRolesByRoleName(roleName);
            if (roles.isEmpty()) {
                throw new EntityNotFoundException("No roles found with name: " + roleName);
            }
            ApiResponse<List<RoleDTO>> response = new ApiResponse<>(HttpStatus.OK.value(), "Roles retrieved successfully", roles);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (EntityNotFoundException ex) {
            throw ex;  // Handled by the Global Exception Handler
        } catch (Exception ex) {
            throw new RuntimeException("An error occurred while retrieving roles by name", ex);  // Handled by the Global Exception Handler
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleDTO>> createRole(@RequestBody RoleDTO roleDTO) {
        try {
            RoleDTO createdRole = roleService.createRole(roleDTO);
            ApiResponse<RoleDTO> response = new ApiResponse<>(HttpStatus.CREATED.value(), "Role created successfully", createdRole);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (InvalidDataException ex) {
            throw ex;  // Handled by the Global Exception Handler
        } catch (Exception ex) {
            throw new RuntimeException("An error occurred while creating the role", ex);  // Handled by the Global Exception Handler
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDTO>> updateRole(@PathVariable Long id, @RequestBody RoleDTO roleDTO) {
        try {
            RoleDTO updatedRole = roleService.updateRole(id, roleDTO);
            ApiResponse<RoleDTO> response = new ApiResponse<>(HttpStatus.OK.value(), "Role updated successfully", updatedRole);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (EntityNotFoundException ex) {
            throw ex;  // Handled by the Global Exception Handler
        } catch (InvalidDataException ex) {
            throw ex;  // Handled by the Global Exception Handler
        } catch (Exception ex) {
            throw new RuntimeException("An error occurred while updating the role", ex);  // Handled by the Global Exception Handler
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            ApiResponse<Void> response = new ApiResponse<>(HttpStatus.NO_CONTENT.value(), "Role deleted successfully", null);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException ex) {
            throw ex;  // Handled by the Global Exception Handler
        } catch (Exception ex) {
            throw new RuntimeException("An error occurred while deleting the role", ex);  // Handled by the Global Exception Handler
        }
    }
}
