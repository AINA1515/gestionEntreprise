package com.itu.gestionRH.controller.hiring;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.repository.hiring.ContratRepository;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import com.itu.gestionRH.repository.organization.PosteRepository;
import com.itu.gestionRH.entity.hiring.Contrat;
import com.itu.gestionRH.entity.enums.ContratActe;
import com.itu.gestionRH.entity.enums.ContratStatut;
import com.itu.gestionRH.entity.enums.ContratType;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.common.BusinessRuleException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1/onboarding")
public class ContratController {
    private final AccessScope accessScope;
    private final ContratRepository contrats;
    private final EmployeRepository employes;
    private final PosteRepository postes;
    public ContratController(AccessScope accessScope, ContratRepository contrats,
            EmployeRepository employes, PosteRepository postes) {
        this.accessScope=accessScope; this.contrats=contrats; this.employes=employes; this.postes=postes;
    }
    public record ContratRequest(Long employeId, Long posteId, String typeContrat,
                                 String typeActe, Long contratPrecedentId, LocalDate dateDebut,
                                 LocalDate dateFin, BigDecimal salaireBrut) {}
    public record ContratResponse(Long id, Long employeId, Long posteId, String typeContrat,
                                  String typeActe, Long contratPrecedentId, LocalDate dateDebut,
                                  LocalDate dateFin, LocalDate dateFinEffective, BigDecimal salaireBrut,
                                  String devise, String statut) {}

    @PreAuthorize("hasAuthority('PERM_CONTRAT_GERER')")
    @PostMapping("/contrats")
    @Transactional
    public ContratResponse creer(@RequestBody ContratRequest request) {
        var employe = employes.findById(request.employeId()).orElseThrow(() -> new NotFoundException("Employé introuvable."));
        ContratType type;
        ContratActe acte;
        try { type = ContratType.valueOf(request.typeContrat()); acte = ContratActe.valueOf(request.typeActe()); }
        catch (RuntimeException ex) { throw new BusinessRuleException("TYPE_CONTRAT_INVALIDE", "Le type de contrat ou d'acte est invalide."); }
        LocalDate debut = request.dateDebut();
        if (debut == null) throw new BusinessRuleException("DATE_DEBUT_REQUISE", "La date de début du contrat est requise.");
        if (request.salaireBrut() == null || request.salaireBrut().signum() < 0)
            throw new BusinessRuleException("SALAIRE_CONTRAT_REQUIS", "Le salaire brut doit être renseigné et positif ou nul.");
        if (type == ContratType.CDI ? request.dateFin() != null : request.dateFin() == null || request.dateFin().isBefore(debut))
            throw new BusinessRuleException("DATES_CONTRAT_INVALIDES", "La date de fin doit être renseignée pour un contrat à durée déterminée et absente pour un CDI.");
        Contrat precedent;
        if (acte == ContratActe.INITIAL) {
            if (request.contratPrecedentId() != null || contrats.findByEmployeIdOrderByDateDebutDescIdDesc(employe.getId()).size() > 0)
                throw new BusinessRuleException("ACTE_INITIAL_INVALIDE", "Un contrat initial ne peut être créé que pour un salarié sans historique contractuel.");
            precedent = null;
        } else {
            precedent = request.contratPrecedentId() == null ? null : contrats.findById(request.contratPrecedentId()).orElse(null);
            if (precedent == null || !precedent.getEmploye().getId().equals(employe.getId()))
                throw new BusinessRuleException("CONTRAT_PRECEDENT_REQUIS", "Sélectionnez un contrat précédent de ce salarié.");
            var historique = contrats.findByEmployeIdOrderByDateDebutDescIdDesc(employe.getId());
            if (historique.isEmpty() || !historique.get(0).getId().equals(precedent.getId()))
                throw new BusinessRuleException("CONTRAT_PRECEDENT_INVALIDE", "Le nouveau contrat doit succéder au contrat le plus récent.");
            if (contrats.findByEmployeIdAndStatutContratIn(employe.getId(),
                    java.util.List.of(ContratStatut.EN_COURS, ContratStatut.EN_ATTENTE)).stream()
                    .anyMatch(c -> !c.getId().equals(precedent.getId())))
                throw new BusinessRuleException("CONTRAT_ACTIF_EXISTANT", "Terminez le contrat courant avant de créer le suivant.");
            if (debut.isBefore(precedent.getDateDebut()))
                throw new BusinessRuleException("DATES_CONTRAT_INVALIDES", "Le nouveau contrat ne peut pas commencer avant le contrat précédent.");
            LocalDate finPrecedent = precedent.getDateFinEffective() != null ? precedent.getDateFinEffective() : precedent.getDateFin();
            if (finPrecedent != null && !debut.isAfter(finPrecedent))
                throw new BusinessRuleException("DATES_CONTRAT_INVALIDES", "Le nouveau contrat doit commencer après la fin du précédent.");
            precedent.setDateFinEffective(debut.minusDays(1).isBefore(precedent.getDateDebut()) ? precedent.getDateDebut() : debut.minusDays(1));
            precedent.setStatutContrat(acte == ContratActe.RENOUVELLEMENT ? ContratStatut.RENOUVELE : ContratStatut.TERMINE);
            contrats.save(precedent);
        }
        var poste = request.posteId() == null && precedent != null ? precedent.getPoste()
                : postes.findById(request.posteId() == null ? -1L : request.posteId()).orElseThrow(() -> new NotFoundException("Poste introuvable."));
        Contrat contrat = Contrat.nouvelle();
        contrat.setEmploye(employe); contrat.setPoste(poste); contrat.setTypeContrat(type); contrat.setTypeActe(acte);
        contrat.setContratPrecedent(precedent); contrat.setDateDebut(debut); contrat.setDateFin(request.dateFin());
        contrat.setSalaireBrut(request.salaireBrut()); contrat.setDevise("MGA");
        contrat.setStatutContrat(debut.isAfter(LocalDate.now()) ? ContratStatut.EN_ATTENTE : ContratStatut.EN_COURS);
        return response(contrats.saveAndFlush(contrat));
    }
    @PreAuthorize("hasAnyAuthority('PERM_CONTRAT_GERER','PERM_DOCUMENT_CONSULTER_PERSO')")
    @GetMapping("/employes/{id}/contrats")
    @Transactional(readOnly = true)
    public List<ContratResponse> historique(@PathVariable Long id) {
        accessScope.requireEmploye(id);
        return contrats.findByEmployeIdOrderByDateDebutDescIdDesc(id).stream().map(this::response).toList();
    }
    private ContratResponse response(Contrat c) { return new ContratResponse(c.getId(), c.getEmploye().getId(), c.getPoste().getId(),
            c.getTypeContrat().name(), c.getTypeActe().name(), c.getContratPrecedent() == null ? null : c.getContratPrecedent().getId(),
            c.getDateDebut(), c.getDateFin(), c.getDateFinEffective(), c.getSalaireBrut(), c.getDevise(), c.getStatutContrat().name()); }
}
