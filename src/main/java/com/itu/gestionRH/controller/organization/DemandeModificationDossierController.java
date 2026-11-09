package com.itu.gestionRH.controller.organization;
import com.itu.gestionRH.entity.organization.*;
import com.itu.gestionRH.repository.organization.*;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import java.time.LocalDateTime; import java.util.*;
import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*; import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/dossier-modifications")
public class DemandeModificationDossierController {
 public record DemandeRequest(Map<String,Object> anciennesValeurs,Map<String,Object> nouvellesValeurs,String motif){}
 public record DecisionRequest(String decision){}
 private final DemandeModificationDossierRepository demandes; private final EmployeRepository employes; private final AffectationRepository affectations; private final UtilisateurRepository utilisateurs; private final AccessScope scope;
 public DemandeModificationDossierController(DemandeModificationDossierRepository d,EmployeRepository e,AffectationRepository a,UtilisateurRepository u,AccessScope s){demandes=d;employes=e;affectations=a;utilisateurs=u;scope=s;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("isAuthenticated()")
 public DemandeModificationDossier creer(@RequestBody DemandeRequest r){
  Long eid=scope.currentUserId()==null?null:utilisateurs.findById(scope.currentUserId()).map(u->u.getEmployeId()).orElse(null); if(eid==null) throw new IllegalStateException("Dossier salarié introuvable");
  var aff=affectations.findFirstByEmployeIdAndDateFinIsNullOrderByDateDebutDesc(eid).orElseThrow(()->new IllegalStateException("Supérieur introuvable"));
  var d=new DemandeModificationDossier(); d.setEmploye(employes.getReferenceById(eid)); d.setSuperieur(aff.getSuperieur()); d.setAnciennesValeurs(r.anciennesValeurs()==null?"{}":r.anciennesValeurs().toString()); d.setNouvellesValeurs(r.nouvellesValeurs()==null?"{}":r.nouvellesValeurs().toString()); d.setMotif(r.motif()); return demandes.save(d);
 }
 @GetMapping("/moi") @PreAuthorize("isAuthenticated()") public List<DemandeModificationDossier> moi(){Long eid=utilisateurs.findById(scope.currentUserId()).map(u->u.getEmployeId()).orElse(null); return eid==null?List.of():demandes.findByEmployeIdOrderByDateDemandeDesc(eid);}
 @GetMapping("/a-valider") @PreAuthorize("hasAnyAuthority('PERM_DEMANDE_CREER','PERM_DOSSIER_CONSULTER_TOUS')") public List<DemandeModificationDossier> aValider(){Long eid=utilisateurs.findById(scope.currentUserId()).map(u->u.getEmployeId()).orElse(null); return eid==null?List.of():demandes.findBySuperieurIdAndStatutOrderByDateDemandeDesc(eid,"EN_ATTENTE");}
 @PostMapping("/{id}/decision") @PreAuthorize("isAuthenticated()") public DemandeModificationDossier decision(@PathVariable Long id,@RequestBody DecisionRequest r){var d=demandes.findById(id).orElseThrow(); Long eid=utilisateurs.findById(scope.currentUserId()).map(u->u.getEmployeId()).orElse(null); var sup=employes.findById(eid).orElseThrow(); if(!sup.getId().equals(d.getSuperieur().getId())) throw new org.springframework.security.access.AccessDeniedException("Supérieur requis"); if(!Set.of("ACCEPTEE","REFUSEE").contains(r.decision())) throw new IllegalArgumentException("Décision invalide"); d.setStatut(r.decision()); d.setDateDecision(LocalDateTime.now()); d.setDecidePar(sup); return demandes.save(d);}
}
