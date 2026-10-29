package com.itu.gestionRH.service.organization;

import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.controller.organization.OrganisationDtos.*;
import com.itu.gestionRH.entity.enums.ContratStatut;
import com.itu.gestionRH.entity.hiring.Affectation;
import com.itu.gestionRH.entity.organization.Employe;
import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.repository.hiring.ContratRepository;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.entity.enums.EmployeStatut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EmployeService {
    private final EmployeRepository employes;
    private final AffectationRepository affectations;
    private final ContratRepository contrats;
    private final UtilisateurRepository utilisateurs;
    private final AccessScope accessScope;
    public EmployeService(EmployeRepository employes, AffectationRepository affectations, ContratRepository contrats,
            UtilisateurRepository utilisateurs, AccessScope accessScope) {
        this.employes=employes; this.affectations=affectations; this.contrats=contrats; this.utilisateurs=utilisateurs; this.accessScope=accessScope;
    }

    public Page<EmployeDto> lister(Long departementId, EmployeStatut statut, String texte, Pageable pageable) {
        String query = texte == null || texte.isBlank() ? null : texte.trim();
        return employes.rechercher(departementId, statut, query, pageable).map(this::dto);
    }

    public EmployeDto obtenir(Long id) { accessScope.requireEmploye(id); return dto(trouver(id)); }

    public EmployeDto moi(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurs.findById(utilisateurId).orElseThrow(() -> new NotFoundException("Fiche salarié introuvable."));
        if (utilisateur.getEmployeId() == null) throw new NotFoundException("Fiche salarié introuvable.");
        return dto(trouver(utilisateur.getEmployeId()));
    }

    public Page<EmployeDto> collaborateurs(Long managerUtilisateurId, Pageable pageable) {
        Utilisateur manager = utilisateurs.findById(managerUtilisateurId).orElseThrow(() -> new NotFoundException("Collaborateurs introuvables."));
        if (manager.getEmployeId() == null) throw new NotFoundException("Collaborateurs introuvables.");
        List<EmployeDto> content = affectations.findBySuperieurIdAndDateFinIsNull(manager.getEmployeId()).stream()
                .map(Affectation::getEmploye).distinct().map(this::dto).toList();
        int start = (int) Math.min(pageable.getOffset(), content.size());
        int end = Math.min(start + pageable.getPageSize(), content.size());
        return new org.springframework.data.domain.PageImpl<>(content.subList(start, end), pageable, content.size());
    }

    private Employe trouver(Long id) { return employes.findById(id).orElseThrow(() -> new NotFoundException("Employé introuvable.")); }
    private EmployeDto dto(Employe e) {
        Affectation current = affectations.findFirstByEmployeIdAndDateFinIsNullOrderByDateDebutDesc(e.getId()).orElse(null);
        var contratsDuSalarie = contrats.findByEmployeIdOrderByDateDebutDescIdDesc(e.getId()).stream()
                .map(c -> new ContratResume(c.getId(), c.getTypeContrat().name(), c.getTypeActe().name(),
                        c.getContratPrecedent() == null ? null : c.getContratPrecedent().getId(),
                        c.getDateDebut(), c.getDateFin(), c.getDateFinEffective(), c.getSalaireBrut(),
                        c.getDevise(), c.getStatutContrat().name())).toList();
        AffectationResume affectation = null;
        Long departementId = null; String departement = null;
        if (current != null) {
            var poste = current.getPoste();
            departementId = poste.getDepartement().getId(); departement = poste.getDepartement().getLibelle();
            affectation = new AffectationResume(poste.getId(), poste.getCodePoste(), poste.getIntitule(),
                    poste.getStatutPoste().name(), current.getDateDebut(),
                    current.getSuperieur() == null ? null : current.getSuperieur().getId());
        }
        return new EmployeDto(e.getId(), e.getMatricule(), e.getNom(), e.getPrenom(), e.getEmailProfessionnel(), e.getTelephone(),
                e.getStatutActivite(), departementId, departement, affectation, contratsDuSalarie);
    }
}
