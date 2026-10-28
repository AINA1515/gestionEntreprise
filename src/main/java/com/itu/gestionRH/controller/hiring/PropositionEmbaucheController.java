package com.itu.gestionRH.controller.hiring;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.repository.hiring.PropositionEmbaucheRepository;
import com.itu.gestionRH.security.AccessScope;
import org.springframework.transaction.annotation.Transactional;
import com.itu.gestionRH.entity.enums.ContratType;
import com.itu.gestionRH.entity.enums.CandidatureStatut;
import com.itu.gestionRH.entity.enums.PropositionStatut;
import com.itu.gestionRH.entity.hiring.PropositionEmbauche;
import com.itu.gestionRH.repository.recruitment.CandidatureRepository;
import com.itu.gestionRH.workflow.PropositionEmbaucheMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.context.SecurityContextHolder;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.security.jwt.JwtAuthenticationFilter.JwtPrincipal;
import com.itu.gestionRH.service.hiring.ContratService;

/** Confirmation de recrutement (ex-page /rh/confirmation). */
@RestController
@RequestMapping("/api/v1/embauche")
public class PropositionEmbaucheController {
    private final PropositionEmbaucheRepository propositions;
    private final AccessScope accessScope;
    private final CandidatureRepository candidatures;
    private final PropositionEmbaucheMachine machine;
    private final UtilisateurRepository utilisateurs;
    private final ContratService contratService;
    public PropositionEmbaucheController(PropositionEmbaucheRepository propositions, AccessScope accessScope,
            CandidatureRepository candidatures, PropositionEmbaucheMachine machine, UtilisateurRepository utilisateurs,
            ContratService contratService) {
        this.propositions=propositions; this.accessScope=accessScope; this.candidatures=candidatures; this.machine=machine;
        this.utilisateurs=utilisateurs; this.contratService=contratService;
    }

    /** typeContrat : CDD | CDI | STAGE | ALTERNANCE (contrainte CK_PROP_TYPE). */
    public record ConfirmationRequest(Long candidatureId, String typeContrat,
                                      LocalDate dateDebut, String message) {}

    public record PropositionRequest(@NotNull Long candidatureId, @NotNull ContratType typeContrat,
            @DecimalMin("0.00") java.math.BigDecimal salaireBrut, LocalDate dateDebut, LocalDate dateFin,
            @NotNull LocalDate dateLimiteReponse) {}

    public record ConfirmationResponse(Long id, Long candidatureId, String poste, String typeContrat,
            java.math.BigDecimal salaireBrut, LocalDate dateDebut, LocalDate dateFin, LocalDate dateEnvoi,
            LocalDate dateLimiteReponse, String statut, List<String> actionsDisponibles,
            Long employeId, String matricule) {}
    public record ReponseRequest(String statut, String commentaire) {}
    public record EmployeConversionResponse(Long employeId, Long candidatId, String matricule, String statut) {}

    @PreAuthorize("hasAuthority('PERM_PROPOSITION_EMETTRE')")
    @PostMapping("/propositions")
    @Transactional
    public ConfirmationResponse confirmer(@Valid @RequestBody PropositionRequest request) {
        var candidature = accessScope.requireCandidature(request.candidatureId());
        if (candidature.getStatutCandidature() != CandidatureStatut.RETENUE)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La candidature doit être retenue avant l’envoi d’une proposition.");
        if (request.dateLimiteReponse().isBefore(LocalDate.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date limite de réponse doit être aujourd’hui ou ultérieure.");
        LocalDate dateDebut = request.dateDebut() == null ? LocalDate.now() : request.dateDebut();
        if (request.typeContrat() == ContratType.CDI && request.dateFin() != null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un contrat CDI ne doit pas avoir de date de fin.");
        if (request.typeContrat() != ContratType.CDI && (request.dateFin() == null || request.dateFin().isBefore(dateDebut)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une date de fin valide est requise pour ce type de contrat.");
        var existante = propositions.findByCandidatureId(candidature.getId());
        if (existante != null && existante.getStatutProposition() != PropositionStatut.ANNULEE
                && existante.getStatutProposition() != PropositionStatut.REFUSEE
                && existante.getStatutProposition() != PropositionStatut.EXPIREE)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une proposition active existe déjà pour cette candidature.");
        PropositionEmbauche proposition = PropositionEmbauche.nouvelle();
        proposition.setCandidature(candidature);
        Long userId = ((JwtPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).userId();
        proposition.setRedacteur(utilisateurs.findById(userId).orElseThrow(() -> new NotFoundException("Utilisateur introuvable.")));
        proposition.setTypeContratPropose(request.typeContrat());
        var salaire = request.salaireBrut() != null ? request.salaireBrut()
                : candidature.getSalairePropose() != null ? candidature.getSalairePropose()
                : candidature.getOffre().getDemande().getBudgetAlloue();
        if (salaire == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le salaire brut doit être renseigné ou provenir du budget de la demande.");
        proposition.setSalaireBrutPropose(salaire);
        proposition.setDateDebutSouhaitee(dateDebut);
        proposition.setDateFinSouhaitee(request.dateFin());
        proposition.setDateEnvoi(LocalDate.now());
        proposition.setDateLimiteReponse(request.dateLimiteReponse());
        proposition.appliquerTransitionWorkflow(PropositionStatut.BROUILLON);
        propositions.save(proposition);
        machine.transition(proposition, "envoyer", WorkflowContext.utilisateurCourant("Proposition d’embauche envoyée"));
        return versReponse(proposition);
    }

    @PreAuthorize("hasAuthority('PERM_PROPOSITION_REPONDRE')")
    @PatchMapping("/propositions/{id}/reponse")
    @Transactional
    public ConfirmationResponse repondre(@PathVariable Long id, @RequestBody ReponseRequest request) {
        var proposition=propositions.findById(id).orElseThrow(()->new NotFoundException("Proposition introuvable."));
        accessScope.requirePropositionOwner(proposition);
        if (proposition.getStatutProposition() != PropositionStatut.ENVOYEE || proposition.getDateLimiteReponse().isBefore(LocalDate.now()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette proposition n’accepte plus de réponse.");
        String action = "ACCEPTER".equalsIgnoreCase(request.statut()) || "ACCEPTEE".equalsIgnoreCase(request.statut()) ? "accepter" : "REFUSER".equalsIgnoreCase(request.statut()) || "REFUSEE".equalsIgnoreCase(request.statut()) ? "refuser" : null;
        if (action == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La réponse doit être accepter ou refuser.");
        if (action.equals("refuser") && (request.commentaire() == null || request.commentaire().isBlank()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un motif de refus est obligatoire.");
        proposition.setMotifRefus(action.equals("refuser") ? request.commentaire().trim() : null);
        proposition.setDateReponse(LocalDate.now());
        machine.transition(proposition, action, WorkflowContext.utilisateurCourant(request.commentaire()));
        if (action.equals("accepter")) contratService.finaliserEmbauche(proposition);
        return versReponse(proposition);
    }

    @PreAuthorize("hasAuthority('PERM_PROPOSITION_EMETTRE')")
    @GetMapping("/propositions")
    @Transactional(readOnly = true)
    public List<ConfirmationResponse> lister() { return propositions.findAll().stream().map(this::versReponse).toList(); }

    @PreAuthorize("hasAnyAuthority('PERM_PROPOSITION_EMETTRE','PERM_PROPOSITION_REPONDRE')")
    @GetMapping("/propositions/mes")
    @Transactional(readOnly = true)
    public List<ConfirmationResponse> mesPropositions(@RequestParam(required = false) Long candidatId) {
        Long id = accessScope.hasPermission("PROPOSITION_EMETTRE") ? candidatId : accessScope.requireCandidatCourant();
        if (id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le candidat est requis.");
        return propositions.findByCandidatureCandidatId(id, org.springframework.data.domain.Pageable.unpaged()).getContent()
                .stream().map(this::versReponse).toList();
    }

    private ConfirmationResponse versReponse(PropositionEmbauche p) {
        return new ConfirmationResponse(p.getId(), p.getCandidature().getId(), p.getCandidature().getOffre().getTitre(),
                p.getTypeContratPropose().name(), p.getSalaireBrutPropose(), p.getDateDebutSouhaitee(), p.getDateFinSouhaitee(), p.getDateEnvoi(),
                p.getDateLimiteReponse(), p.getStatutProposition().name(), machine.actionsDisponibles(p, WorkflowContext.utilisateurCourant(null)),
                p.getEmploye() == null ? null : p.getEmploye().getId(), p.getEmploye() == null ? null : p.getEmploye().getMatricule());
    }

    @PreAuthorize("hasAuthority('PERM_EMPLOYE_CONVERTIR')")
    @org.springframework.web.bind.annotation.PostMapping("/candidats/{id}/convertir-employe")
    @Transactional
    public EmployeConversionResponse convertir(@PathVariable Long id) {
        var proposition = propositions.findFirstByCandidatureCandidatIdAndStatutPropositionOrderByDateReponseDesc(id, PropositionStatut.ACCEPTEE)
                .orElseThrow(() -> new NotFoundException("Aucune proposition acceptée pour ce candidat."));
        var employe = contratService.finaliserEmbauche(proposition);
        return new EmployeConversionResponse(employe.getId(), id, employe.getMatricule(), employe.getStatutActivite().name());
    }
}
