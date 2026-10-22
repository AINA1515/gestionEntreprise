package com.itu.gestionRH.service.security;

import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.entity.enums.CompteStatut;
import com.itu.gestionRH.entity.enums.CompteType;
import com.itu.gestionRH.entity.security.*;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import com.itu.gestionRH.repository.security.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminService {
    public record UtilisateurView(Long id, String login, String email, String nomAffichage,
            String typeCompte, String statutCompte, Long employeId, List<String> roles) {}
    public record UtilisateurCreation(@NotBlank @Size(max=100) String login,
            @Email @NotBlank @Size(max=150) String email, @NotBlank String nomAffichage,
            @NotBlank @Size(min=12,max=72) String motDePasse, Long employeId,
            @NotEmpty List<@NotBlank String> roles) {}
    public record UtilisateurPatch(CompteStatut statutCompte,
            @Size(min=12,max=72) String nouveauMotDePasse, Long employeId) {}
    public record RoleView(String code,String libelle,String description,boolean actif) {}
    public record PermissionView(String code,String libelle,String module) {}
    public record RolesRequest(@NotEmpty List<@NotBlank String> roles) {}

    private final UtilisateurRepository utilisateurs;
    private final UtilisateurRoleRepository attributions;
    private final RoleRhRepository roles;
    private final PermissionRepository permissions;
    private final RolePermissionRepository rolePermissions;
    private final EmployeRepository employes;
    private final PasswordEncoder encoder;

    public AdminService(UtilisateurRepository utilisateurs, UtilisateurRoleRepository attributions,
            RoleRhRepository roles, PermissionRepository permissions, RolePermissionRepository rolePermissions,
            EmployeRepository employes, PasswordEncoder encoder) {
        this.utilisateurs=utilisateurs; this.attributions=attributions; this.roles=roles;
        this.permissions=permissions; this.rolePermissions=rolePermissions; this.employes=employes; this.encoder=encoder;
    }

    @Transactional(readOnly=true)
    public List<UtilisateurView> utilisateurs(){return utilisateurs.findAll().stream().map(this::view).toList();}

    public UtilisateurView creer(UtilisateurCreation req){
        if(utilisateurs.existsByEmailIgnoreCase(req.email())) throw new BusinessRuleException("EMAIL_DEJA_UTILISE","Cette adresse e-mail est déjà utilisée.");
        if(utilisateurs.existsByLoginIgnoreCase(req.login())) throw new BusinessRuleException("LOGIN_DEJA_UTILISE","Ce login est déjà utilisé.");
        Utilisateur u=Utilisateur.nouvelle(); u.setLogin(req.login());u.setEmail(req.email());u.setNomAffichage(req.nomAffichage());
        u.setMotDePasseHash(encoder.encode(req.motDePasse()));u.setTypeCompte(CompteType.INTERNE);u.setStatutCompte(CompteStatut.ACTIF);u.setNbEchecsConnexion(0);
        if(req.employeId()!=null)u.setEmploye(employes.findById(req.employeId()).orElseThrow(()->new NotFoundException("Employé introuvable.")));
        utilisateurs.save(u);replaceRoles(u,req.roles());return view(u);
    }

    public UtilisateurView modifier(Long id,UtilisateurPatch req){
        Utilisateur u=requireUser(id);if(req.statutCompte()!=null)u.setStatutCompte(req.statutCompte());
        if(req.nouveauMotDePasse()!=null)u.setMotDePasseHash(encoder.encode(req.nouveauMotDePasse()));
        if(req.employeId()!=null)u.setEmploye(employes.findById(req.employeId()).orElseThrow(()->new NotFoundException("Employé introuvable.")));
        return view(u);
    }
    public List<String> rolesUtilisateur(Long id){return activeRoles(requireUser(id));}
    public List<String> remplacerRoles(Long id,RolesRequest req){Utilisateur u=requireUser(id);replaceRoles(u,req.roles());return activeRoles(u);}
    @Transactional(readOnly=true)
    public List<RoleView> roles(){return roles.findAll().stream().map(r->new RoleView(r.getCode(),r.getLibelle(),r.getDescription(),Boolean.TRUE.equals(r.getEstActif()))).toList();}
    @Transactional(readOnly=true)
    public List<PermissionView> permissions(){return permissions.findAll().stream().map(p->new PermissionView(p.getCode(),p.getLibelle(),p.getModule())).toList();}
    public List<String> remplacerPermissions(String code,List<String> codes){
        RoleRh role=roles.findByCode(code);if(role==null)throw new NotFoundException("Rôle introuvable.");
        Set<String> distinct=Set.copyOf(codes);List<Permission> found=permissions.findAll().stream().filter(p->distinct.contains(p.getCode())).toList();
        if(found.size()!=distinct.size())throw new NotFoundException("Une permission demandée est introuvable.");
        rolePermissions.deleteByRoleId(role.getId());rolePermissions.saveAll(found.stream().map(p->{RolePermission rp=RolePermission.nouvelle();rp.setRole(role);rp.setPermission(p);return rp;}).toList());
        return found.stream().map(Permission::getCode).sorted().toList();
    }

    private Utilisateur requireUser(Long id){return utilisateurs.findById(id).orElseThrow(()->new NotFoundException("Utilisateur introuvable."));}
    private List<String> activeRoles(Utilisateur u){return attributions.findByUtilisateurIdAndDateFinIsNullAndDateDebutLessThanEqual(u.getId(),LocalDate.now()).stream().map(a->a.getRole().getCode()).distinct().sorted().toList();}
    private void replaceRoles(Utilisateur u,List<String> codes){
        Set<String> desired=Set.copyOf(codes);List<RoleRh> found=roles.findAll().stream().filter(r->desired.contains(r.getCode())&&Boolean.TRUE.equals(r.getEstActif())).toList();
        if(found.size()!=desired.size())throw new NotFoundException("Un rôle demandé est introuvable ou inactif.");
        LocalDate today=LocalDate.now();var active=attributions.findByUtilisateurIdAndDateFinIsNullAndDateDebutLessThanEqual(u.getId(),today);
        Set<Long>wanted=found.stream().map(RoleRh::getId).collect(Collectors.toSet());Set<Long>existing=active.stream().map(a->a.getRole().getId()).collect(Collectors.toSet());
        active.stream().filter(a->!wanted.contains(a.getRole().getId())).forEach(a->a.terminer(today,"Mise à jour par administration"));
        found.stream().filter(r->!existing.contains(r.getId())).forEach(r->{UtilisateurRole a=UtilisateurRole.nouvelle();a.setUtilisateur(u);a.setRole(r);a.setDateDebut(today);attributions.save(a);});
    }
    private UtilisateurView view(Utilisateur u){return new UtilisateurView(u.getId(),u.getLogin(),u.getEmail(),u.getNomAffichage(),u.getTypeCompte().name(),u.getStatutCompte().name(),u.getEmployeId(),activeRoles(u));}
}
