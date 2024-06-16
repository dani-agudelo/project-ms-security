package com.ucaldas.mssecurity.Controllers;

import com.ucaldas.mssecurity.Models.Permission;
import com.ucaldas.mssecurity.Models.Role;
import com.ucaldas.mssecurity.Models.RolePermission;
import com.ucaldas.mssecurity.Repositories.PermissionRepository;
import com.ucaldas.mssecurity.Repositories.RolePermissionRepository;
import com.ucaldas.mssecurity.Repositories.RoleRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/role_permission")
public class RolePermissionController {
  @Autowired private RoleRepository theRoleRepository;

  @Autowired private PermissionRepository thePermissionRepository;

  @Autowired private RolePermissionRepository theRolePermissionRepository;

  /** Representa la asociación entre un Rol y un Permiso */
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("role/{roleId}/permission/{permissionId}")
  public RolePermission create(
      @PathVariable String roleId,
      @PathVariable String permissionId,
      final HttpServletResponse response) {
    Role theRole = this.theRoleRepository.findById(roleId).orElse(null);
    Permission thePermission = this.thePermissionRepository.findById(permissionId).orElse(null);

    if (theRole != null && thePermission != null) {
      if (this.theRolePermissionRepository.getRolePermission(
              theRole.get_id(), thePermission.get_id())
          != null) {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        return null;
      }

      RolePermission newRolePermission = new RolePermission();
      newRolePermission.setRole(theRole);
      newRolePermission.setPermission(thePermission);
      return this.theRolePermissionRepository.save(newRolePermission);
    } else {
      return null;
    }
  }

  /**
   * Elimina la asociación entre un Rol y un Permiso por su id
   *
   * @param id
   */
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("{id}")
  public void delete(@PathVariable String id) {
    RolePermission theRolePermission = this.theRolePermissionRepository.findById(id).orElse(null);
    if (theRolePermission != null) {
      this.theRolePermissionRepository.delete(theRolePermission);
    }
  }

  /**
   * Devuelve todos los permisos asociados a un rol
   *
   * @param roleId
   * @return
   */
  @GetMapping("role/{roleId}")
  public List<RolePermission> findByRole(@PathVariable String roleId) {
    List<RolePermission> rolePermissions =
        this.theRolePermissionRepository.getPermissionsByRole(roleId);

    return rolePermissions.stream()
        .map(
            rolePermission -> {
              rolePermission.setRole(null);
              return rolePermission;
            })
        .toList();
  }
}
