package com.itu.gestionRH.controller.recruitment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.itu.gestionRH.entity.recruitment.DemandeRecrutement;
import com.itu.gestionRH.repository.recruitment.DemandeRecrutementRepository;

/** Espace manager : tableau de bord, mes demandes, nouvelle demande. */
@RestController
@RequestMapping("/api/manager")
public class DemandeRecrutementController {

    public record DemandeResponse(Long id, Long posteId, String poste, String departement,
                                  Integer effectif, String motif, BigDecimal budget,
                                  LocalDate dateSouhaitee, String statut, LocalDateTime dateDemande) {}

    public record DashboardResponse(long nombreDemandes, List<DemandeResponse> recentes) {}

    public record NouvelleDemandeRequest(Long posteId, Integer effectif, String motif,
                                         BigDecimal budget, LocalDate dateSouhaitee) {}

    private final DemandeRecrutementRepository demandeRepository;

    public DemandeRecrutementController(DemandeRecrutementRepository demandeRepository) {
        this.demandeRepository = demandeRepository;
    }

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        List<DemandeResponse> demandes = mesDemandes();
        return new DashboardResponse(demandes.size(), demandes.stream().limit(5).toList());
    }

    @GetMapping("/demandes")
    @Transactional(readOnly = true)
    public List<DemandeResponse> mesDemandes() {
        // TODO : filtrer sur le manager connecte (DemandeRecrutement.manager) une fois l'auth en place
        return demandeRepository.findAll().stream()
                .sorted(Comparator.comparing(DemandeRecrutement::getDateCreation,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(DemandeRecrutementController::toResponse)
                .toList();
    }

    @PostMapping("/demandes")
    public DemandeResponse creer(@RequestBody NouvelleDemandeRequest request) {
        // TODO : DemandeRecrutementService.creer(request, managerConnecte)
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Creation de demande a implementer");
    }

    private static DemandeResponse toResponse(DemandeRecrutement d) {
        var poste = d.getPoste();
        return new DemandeResponse(d.getId(),
                poste != null ? poste.getId() : null,
                poste != null ? poste.getIntitule() : null,
                poste != null && poste.getDepartement() != null ? poste.getDepartement().getLibelle() : null,
                d.getEffectifDemande(), d.getMotif(), d.getBudgetAlloue(), d.getDateSouhaitee(),
                d.getStatutValidation(), d.getDateCreation());
    }
}
