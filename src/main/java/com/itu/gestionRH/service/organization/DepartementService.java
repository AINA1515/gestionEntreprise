package com.itu.gestionRH.service.organization;

import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.controller.organization.OrganisationDtos.*;
import com.itu.gestionRH.entity.organization.Departement;
import com.itu.gestionRH.entity.organization.DepartementResponsable;
import com.itu.gestionRH.entity.organization.Employe;
import com.itu.gestionRH.repository.organization.DepartementRepository;
import com.itu.gestionRH.repository.organization.DepartementResponsableRepository;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DepartementService {
    private final DepartementRepository departements;
    private final DepartementResponsableRepository responsables;
    private final EmployeRepository employes;

    public DepartementService(DepartementRepository departements, DepartementResponsableRepository responsables, EmployeRepository employes) {
        this.departements = departements; this.responsables = responsables; this.employes = employes;
    }

    @Transactional(readOnly = true)
    public Page<DepartementDto> lister(Boolean actif, Pageable pageable) {
        Page<Departement> page = actif == null ? departements.findAll(pageable) : departements.findByEstActif(actif, pageable);
        return page.map(this::dto);
    }

    @Transactional(readOnly = true)
    public DepartementDto obtenir(Long id) { return dto(trouver(id)); }

    public DepartementDto creer(DepartementRequest request) {
        if (departements.findByCode(request.code()) != null) throw new BusinessRuleException("DEPARTEMENT_CODE_EXISTANT", "Ce code département existe déjà.");
        Departement d = Departement.nouvelle();
        d.setCode(request.code().trim()); d.setLibelle(request.libelle().trim());
        d.setEstActif(request.estActif() == null ? true : request.estActif());
        d.setDepartementParentId(request.departementParentId());
        if (request.responsableId() != null) d.setResponsableId(requireEmploye(request.responsableId()).getId());
        return dto(departements.saveAndFlush(d));
    }

    public DepartementDto modifier(Long id, DepartementRequest request) {
        Departement d = trouver(id);
        if (departements.findByCode(request.code()) != null && !d.getId().equals(departements.findByCode(request.code()).getId()))
            throw new BusinessRuleException("DEPARTEMENT_CODE_EXISTANT", "Ce code département existe déjà.");
        d.setCode(request.code().trim()); d.setLibelle(request.libelle().trim());
        if (request.estActif() != null) d.setEstActif(request.estActif());
        d.setDepartementParentId(request.departementParentId());
        if (request.responsableId() != null && !request.responsableId().equals(d.getResponsableId()))
            d.setResponsableId(requireEmploye(request.responsableId()).getId());
        return dto(departements.saveAndFlush(d));
    }

    public DepartementDto changerResponsable(Long id, ChangementResponsableRequest request) {
        Departement d = trouver(id);
        Employe nouveau = requireEmploye(request.employeId());
        if (nouveau.getId().equals(d.getResponsableId())) return dto(d);
        d.setResponsableId(nouveau.getId());
        departements.saveAndFlush(d); // Le trigger clôture l'ancienne ligne et ouvre la nouvelle.
        responsables.findByDepartementIdOrderByDateDebutDesc(id).stream().filter(r -> r.getDateFin() == null).findFirst()
                .ifPresent(r -> { r.setMotif(request.motif()); responsables.save(r); });
        return dto(d);
    }

    public void desactiver(Long id) { Departement d = trouver(id); d.setEstActif(false); }

    private Departement trouver(Long id) { return departements.findById(id).orElseThrow(() -> new NotFoundException("Département introuvable.")); }
    private Employe requireEmploye(Long id) { return employes.findById(id).orElseThrow(() -> new NotFoundException("Employé responsable introuvable.")); }

    private DepartementDto dto(Departement d) {
        var historique = responsables.findByDepartementIdOrderByDateDebutDesc(d.getId()).stream().map(this::responsableDto).toList();
        var courant = historique.stream().filter(r -> r.dateFin() == null).findFirst().orElse(null);
        return new DepartementDto(d.getId(), d.getCode(), d.getLibelle(), d.getEstActif(), d.getDepartementParentId(), courant, historique);
    }
    private ResponsableDto responsableDto(DepartementResponsable r) {
        Employe e = r.getEmploye();
        return new ResponsableDto(e.getId(), e.getMatricule(), e.getNom(), e.getPrenom(), r.getDateDebut(), r.getDateFin(), r.getMotif());
    }
}
