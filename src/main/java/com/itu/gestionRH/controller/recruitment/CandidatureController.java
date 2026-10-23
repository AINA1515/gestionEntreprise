package com.itu.gestionRH.controller.recruitment;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.itu.gestionRH.entity.security.NiveauFormation;
import com.itu.gestionRH.repository.security.NiveauFormationRepository;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.itu.gestionRH.entity.recruitment.Candidature;
import com.itu.gestionRH.entity.enums.CandidatureStatut;
import com.itu.gestionRH.entity.enums.OffreStatut;
import com.itu.gestionRH.repository.recruitment.OffreEmploiRepository;
import com.itu.gestionRH.repository.recruitment.CandidatureRepository;
import com.itu.gestionRH.repository.security.CandidatRepository;
import com.itu.gestionRH.workflow.CandidatureMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import com.itu.gestionRH.security.AccessScope;

/** Candidatures : depot cote candidat, suivi cote RH. */
@RestController
@RequestMapping("/api/v1")
public class CandidatureController {

    public record CandidatureResponse(Long id, String candidat, String email, String poste,
                                      LocalDate datePostulation, String statut, BigDecimal salairePropose,
                                      BigDecimal salaireReference, List<String> actionsDisponibles,
                                      String niveauFormationCandidat, String niveauFormationRequis,
                                      BigDecimal plafondSalairePreselection, Boolean preselectionnee,
                                      String motifPreselection) {}

    public record StatsCandidatures(long recues, long entretiensAOrganiser, long retenues) {}

    public record SuiviCandidaturesResponse(StatsCandidatures stats, List<CandidatureResponse> candidatures) {}

    public record DepotCandidatureRequest(Long offreId,
            @DecimalMin("0.00") @Digits(integer=13, fraction=2) BigDecimal salairePropose) {}
    public record StatutRequest(String statut, String commentaire) {}

    private final CandidatureRepository candidatureRepository;
    private final CandidatureMachine machine;
    private final AccessScope accessScope;
    private final OffreEmploiRepository offreRepository;
    private final CandidatRepository candidatRepository;
    private final NiveauFormationRepository niveauFormationRepository;
    private final ObjectMapper objectMapper;

    public CandidatureController(CandidatureRepository candidatureRepository, CandidatureMachine machine, AccessScope accessScope,
                                 OffreEmploiRepository offreRepository, CandidatRepository candidatRepository,
                                 NiveauFormationRepository niveauFormationRepository, ObjectMapper objectMapper) {
        this.candidatureRepository = candidatureRepository;
        this.machine = machine;
        this.accessScope = accessScope;
        this.offreRepository = offreRepository;
        this.candidatRepository = candidatRepository;
        this.niveauFormationRepository = niveauFormationRepository;
        this.objectMapper = objectMapper;
    }

    /** Ex-page /rh/candidatures. */
    @PreAuthorize("hasAuthority('PERM_CANDIDATURE_CONSULTER')")
    @GetMapping("/recrutement/candidatures/synthese")
    @Transactional(readOnly = true)
    public SuiviCandidaturesResponse suivi() {
        List<Candidature> toutes = candidatureRepository.findAll().stream().filter(accessScope::canSeeCandidature).toList();
        List<Candidature> actives = toutes.stream()
                .filter(c -> c.getStatutCandidature() != CandidatureStatut.ANNULEE).toList();
        StatsCandidatures stats = new StatsCandidatures(
                actives.size(),
                actives.stream().filter(c -> c.getStatutCandidature() == CandidatureStatut.PRESELECTIONNEE).count(),
                actives.stream().filter(c -> c.getStatutCandidature() == CandidatureStatut.RETENUE).count());
        return new SuiviCandidaturesResponse(stats, toutes.stream().map(this::toResponse).toList());
    }

    /** Ex-page /candidat/candidature (depot d'une candidature). */
    @PreAuthorize("hasAuthority('PERM_CANDIDATURE_POSTULER')")
    @PostMapping("/recrutement/candidatures")
    @Transactional
    public CandidatureResponse deposer(@Valid @RequestBody DepotCandidatureRequest request) {
        Long candidatId = accessScope.requireCandidatCourant();
        if (request == null || request.offreId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L’offre à laquelle postuler est obligatoire.");
        var candidat = candidatRepository.findById(candidatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidat introuvable."));
        if ((candidat.getCvUrl() == null || candidat.getCvUrl().isBlank())
                && (candidat.getCvContenu() == null || candidat.getCvContenu().isBlank()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Créez ou importez un CV avant de postuler.");
        var offre = offreRepository.findById(request.offreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre introuvable."));
        if (offre.getStatutOffre() != OffreStatut.PUBLIEE || offre.getDateCloture() != null
                || (offre.getDateLimiteCandidature() != null && offre.getDateLimiteCandidature().isBefore(LocalDate.now())))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette offre n’accepte plus de candidatures.");
        Candidature existante = candidatureRepository.findByOffreIdAndCandidatId(offre.getId(), candidatId);
        if (existante != null) {
            if (existante.getStatutCandidature() != CandidatureStatut.ANNULEE)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous avez déjà postulé à cette offre.");
            existante.setDatePostulation(LocalDate.now());
            existante.setSalairePropose(request.salairePropose());
            existante.setMotifRejet(null);
            existante.appliquerTransitionWorkflow(CandidatureStatut.RECUE);
            return toResponse(candidatureRepository.save(existante));
        }
        Candidature candidature = Candidature.nouvelle();
        candidature.setOffre(offre);
        candidature.setCandidat(candidat);
        candidature.setDatePostulation(LocalDate.now());
        candidature.setSalairePropose(request.salairePropose());
        candidature.appliquerTransitionWorkflow(CandidatureStatut.RECUE);
        return toResponse(candidatureRepository.save(candidature));
    }

    @PreAuthorize("hasAuthority('PERM_CANDIDATURE_CONSULTER')")
    @GetMapping("/recrutement/candidatures")
    @Transactional(readOnly = true)
    public List<CandidatureResponse> lister(@RequestParam(required = false) Long offreId,
                                            @RequestParam(required = false) String statut) {
        List<Candidature> resultat = candidatureRepository.findAll().stream()
                .filter(accessScope::canSeeCandidature)
                .filter(c -> offreId == null || (c.getOffre() != null && offreId.equals(c.getOffre().getId())))
                .filter(c -> statut == null || statut.equals(c.getStatutCandidature().name()))
                .toList();
        Map<String, NiveauFormation> niveaux = offreId == null ? Map.of() : niveauFormationRepository.findAll().stream()
                .collect(Collectors.toMap(NiveauFormation::getCode, Function.identity()));
        return resultat.stream().map(c -> offreId == null ? toResponse(c) : toResponse(c, evaluerPreselection(c, niveaux))).toList();
    }

    @PreAuthorize("hasAuthority('PERM_CANDIDATURE_PRESELECTIONNER')")
    @PatchMapping("/recrutement/candidatures/{id}/statut")
    public CandidatureResponse changerStatut(@PathVariable Long id, @RequestBody StatutRequest request) {
        accessScope.requireCandidature(id);
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Modification du statut de candidature à implémenter");
    }

    private CandidatureResponse toResponse(Candidature c) {
        return toResponse(c, null);
    }

    private CandidatureResponse toResponse(Candidature c, EvaluationPreselection evaluation) {
        var cand = c.getCandidat();
        String nom = cand == null ? null : ((cand.getPrenom() == null ? "" : cand.getPrenom() + " ")
                + (cand.getNom() == null ? "" : cand.getNom())).trim();
        return new CandidatureResponse(c.getId(), nom, cand != null ? cand.getEmail() : null,
                c.getOffre() != null ? c.getOffre().getTitre() : null,
                c.getDatePostulation(), c.getStatutCandidature().name(), c.getSalairePropose(),
                c.getOffre() == null || c.getOffre().getDemande() == null ? null : c.getOffre().getDemande().getBudgetAlloue(),
                machine.actionsDisponibles(c, WorkflowContext.utilisateurCourant(null)),
                evaluation == null ? null : evaluation.niveauCandidat(),
                evaluation == null ? null : evaluation.niveauRequis(),
                evaluation == null ? null : evaluation.plafondSalaire(),
                evaluation == null ? null : evaluation.retenue(),
                evaluation == null ? null : evaluation.motif());
    }

    private record EvaluationPreselection(String niveauCandidat, String niveauRequis,
                                          BigDecimal plafondSalaire, boolean retenue, String motif) {}

    private EvaluationPreselection evaluerPreselection(Candidature candidature, Map<String, NiveauFormation> niveaux) {
        var demande = candidature.getOffre() == null ? null : candidature.getOffre().getDemande();
        NiveauFormation requis = demande == null ? null : demande.getNiveauFormation();
        String niveauCandidatCode = null;
        if (candidature.getCandidat() != null && candidature.getCandidat().getCvContenu() != null) {
            try {
                JsonNode formations = objectMapper.readTree(candidature.getCandidat().getCvContenu()).path("formations");
                if (formations.isArray()) {
                    niveauCandidatCode = StreamSupport.stream(formations.spliterator(), false)
                            .map(f -> f.path("niveau").asText(null)).filter(code -> code != null && niveaux.containsKey(code))
                            .max(Comparator.comparingInt(code -> niveaux.get(code).getOrdre())).orElse(null);
                }
            } catch (Exception ignored) { /* CV importé ou contenu non exploitable : aucun niveau structuré. */ }
        }
        NiveauFormation niveauCandidat = niveauCandidatCode == null ? null : niveaux.get(niveauCandidatCode);
        String libelleCandidat = niveauCandidat == null ? null : niveauCandidat.getLibelle();
        String libelleRequis = requis == null ? null : requis.getLibelle();
        BigDecimal budget = demande == null ? null : demande.getBudgetAlloue();
        BigDecimal plafond = budget == null ? null : budget.multiply(new BigDecimal("1.10"));
        BigDecimal salaire = candidature.getSalairePropose() != null ? candidature.getSalairePropose() : budget;

        boolean niveauOk = requis == null || (niveauCandidat != null && niveauCandidat.getOrdre() >= requis.getOrdre());
        boolean salaireOk = plafond != null && salaire != null && salaire.compareTo(plafond) <= 0;
        boolean statutEligible = candidature.getStatutCandidature() != CandidatureStatut.ANNULEE
                && candidature.getStatutCandidature() != CandidatureStatut.REJETEE;
        boolean retenue = niveauOk && salaireOk && statutEligible;
        String motif = !statutEligible ? "Candidature annulée ou rejetée"
                : !niveauOk ? "Niveau de formation inférieur ou non renseigné"
                : !salaireOk ? (plafond == null ? "Budget de l’offre non renseigné" : "Salaire supérieur au plafond de présélection")
                : "Niveau et salaire conformes aux critères";
        return new EvaluationPreselection(libelleCandidat, libelleRequis, plafond, retenue, motif);
    }
}
