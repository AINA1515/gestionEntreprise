package com.itu.gestionRH.controller.security;

import com.itu.gestionRH.service.security.AdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAuthority('PERM_UTILISATEUR_ADMIN')")
public class AdminController {
    private final AdminService service;
    public AdminController(AdminService service) { this.service=service; }

    @GetMapping("/utilisateurs")
    public List<AdminService.UtilisateurView> utilisateurs() { return service.utilisateurs(); }

    @PostMapping("/utilisateurs")
    public AdminService.UtilisateurView creer(@Valid @RequestBody AdminService.UtilisateurCreation req) { return service.creer(req); }

    @PatchMapping("/utilisateurs/{id}")
    public AdminService.UtilisateurView modifier(@PathVariable Long id, @Valid @RequestBody AdminService.UtilisateurPatch req) { return service.modifier(id,req); }

    @GetMapping("/utilisateurs/{id}/roles")
    public List<String> rolesUtilisateur(@PathVariable Long id) { return service.rolesUtilisateur(id); }

    @PutMapping("/utilisateurs/{id}/roles")
    public List<String> remplacerRoles(@PathVariable Long id, @Valid @RequestBody AdminService.RolesRequest req) { return service.remplacerRoles(id,req); }

    @GetMapping("/roles")
    public List<AdminService.RoleView> roles() { return service.roles(); }

    @GetMapping("/permissions")
    public List<AdminService.PermissionView> permissions() { return service.permissions(); }

    @PutMapping("/roles/{code}/permissions")
    public List<String> remplacerPermissions(@PathVariable String code, @NotNull @RequestBody List<@NotBlank String> codes) {
        return service.remplacerPermissions(code,codes);
    }
}
