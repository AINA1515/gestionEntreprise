package com.itu.gestionRH.service.organization;

import com.itu.gestionRH.entity.organization.*;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.repository.organization.*;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.security.AccessScope;
import java.time.*;
import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TempsTravailService {
    private final RegimeTravailRepository regimes; private final PlanningTravailRepository plannings;
    private final CreneauTravailRepository creneaux; private final EmployeRepository employes;
    private final UtilisateurRepository utilisateurs; private final AffectationRepository affectations;
    private final HistoriqueTempsTravailRepository historiques; private final AccessScope scope;
    public TempsTravailService(RegimeTravailRepository r, PlanningTravailRepository p, CreneauTravailRepository c, EmployeRepository e, UtilisateurRepository u, AffectationRepository a, HistoriqueTempsTravailRepository h, AccessScope s) { regimes=r;plannings=p;creneaux=c;employes=e;utilisateurs=u;affectations=a;historiques=h;scope=s; }
    private Employe chefCourant(){var u=utilisateurs.findById(scope.currentUserId()).orElseThrow();if(u.getEmployeId()==null)throw new AccessDeniedException("Votre compte n'est pas lié à un salarié.");return employes.findById(u.getEmployeId()).orElseThrow();}
    private void verifierGestion(Long employeId, Employe chef){var a=affectations.findFirstByEmployeIdAndDateFinIsNullOrderByDateDebutDesc(employeId).orElseThrow();boolean responsable=Objects.equals(a.getPoste().getDepartement().getResponsableId(),chef.getId());boolean superieur=a.getSuperieur()!=null&&Objects.equals(a.getSuperieur().getId(),chef.getId());if(!responsable&&!superieur)throw new AccessDeniedException("Vous n'êtes ni le responsable du département ni le supérieur direct de ce salarié.");}
    private void historique(Employe e, PlanningTravail p, String action, String avant, String apres, Employe chef){var h=new HistoriqueTempsTravail();h.setEmploye(e);h.setPlanning(p);h.setAction(action);h.setAncienneValeur(avant);h.setNouvelleValeur(apres);h.setModifiePar(chef);historiques.save(h);}
    @Transactional public PlanningTravail creerPlanning(Long employeId,Long regimeId,LocalDate debut,LocalDate fin,String motif){if(debut==null)throw new IllegalArgumentException("La date de début est obligatoire.");var e=employes.findById(employeId).orElseThrow();var chef=chefCourant();verifierGestion(employeId,chef);var p=new PlanningTravail();p.setEmploye(e);p.setRegime(regimes.findById(regimeId).orElseThrow());p.setDateDebut(debut);p.setDateFin(fin);p.setMotif(motif);p.setAssignePar(chef);p=plannings.save(p);historique(e,p,"CREATION",null,"Planning "+p.getRegime().getCode(),chef);return p;}
    @Transactional public PlanningTravail modifierPlanning(Long id,Long regimeId,LocalDate debut,LocalDate fin,String motif){var p=plannings.findById(id).orElseThrow();var chef=chefCourant();verifierGestion(p.getEmploye().getId(),chef);String avant="Début="+p.getDateDebut()+", fin="+p.getDateFin();p.setRegime(regimes.findById(regimeId).orElseThrow());p.setDateDebut(debut);p.setDateFin(fin);p.setMotif(motif);p=plannings.save(p);historique(p.getEmploye(),p,"MODIFICATION_PLANNING",avant,"Début="+debut+", fin="+fin,chef);return p;}
    @Transactional public void supprimerPlanning(Long id){var p=plannings.findById(id).orElseThrow();var chef=chefCourant();verifierGestion(p.getEmploye().getId(),chef);historique(p.getEmploye(),p,"SUPPRESSION_PLANNING","Planning "+p.getId(),null,chef);plannings.delete(p);}
    @Transactional public CreneauTravail creerCreneau(Long planningId,LocalDateTime debut,LocalDateTime fin,String type,String libelle){var p=plannings.findById(planningId).orElseThrow();var chef=chefCourant();verifierGestion(p.getEmploye().getId(),chef);var c=new CreneauTravail();c.setPlanning(p);c.setDateDebut(debut);c.setDateFin(fin);c.setTypeCreneau(type);c.setLibelle(libelle);c=creneaux.save(c);historique(p.getEmploye(),p,"CREATION_CRENEAU",null,libelle+" "+debut+" - "+fin,chef);return c;}
    @Transactional public CreneauTravail modifierCreneau(Long id,LocalDateTime debut,LocalDateTime fin,String type,String libelle){var c=creneaux.findById(id).orElseThrow();var p=c.getPlanning();var chef=chefCourant();verifierGestion(p.getEmploye().getId(),chef);String avant=c.getLibelle()+" "+c.getDateDebut()+" - "+c.getDateFin();c.setDateDebut(debut);c.setDateFin(fin);c.setTypeCreneau(type);c.setLibelle(libelle);c=creneaux.save(c);historique(p.getEmploye(),p,"MODIFICATION_CRENEAU",avant,libelle+" "+debut+" - "+fin,chef);return c;}
    @Transactional public void supprimerCreneau(Long id){var c=creneaux.findById(id).orElseThrow();var p=c.getPlanning();var chef=chefCourant();verifierGestion(p.getEmploye().getId(),chef);historique(p.getEmploye(),p,"SUPPRESSION_CRENEAU",c.getLibelle()+" "+c.getDateDebut()+" - "+c.getDateFin(),null,chef);creneaux.delete(c);}
    @Transactional public CreneauTravail changerStatutCreneau(Long id,String statut){if(!Set.of("PLANIFIE","REALISE","ABSENT","ANNULE").contains(statut))throw new IllegalArgumentException("Statut de pointage invalide.");var c=creneaux.findById(id).orElseThrow();var p=c.getPlanning();var chef=chefCourant();verifierGestion(p.getEmploye().getId(),chef);String avant=c.getStatut();c.setStatut(statut);c=creneaux.save(c);historique(p.getEmploye(),p,"POINTAGE_CRENEAU",avant,statut,chef);return c;}
}
