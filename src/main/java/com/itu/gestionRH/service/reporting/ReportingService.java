package com.itu.gestionRH.service.reporting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.itu.gestionRH.entity.enums.ContratStatut;
import com.itu.gestionRH.entity.enums.TacheStatut;
import jakarta.persistence.EntityManager;

/** Agrégats de pilotage pour la Direction. Tous les calculs sont faits en base (GROUP BY), jamais en mémoire. */
@Service
public class ReportingService {
    public record Ligne(String libelle, long valeur) {}
    public record LigneMontant(String libelle, BigDecimal montant) {}
    public record Synthese(List<Ligne> demandesParStatut, List<Ligne> entonnoirCandidatures,
            List<Ligne> propositionsParStatut, List<Ligne> effectifsParStatut, List<Ligne> effectifsParDepartement,
            List<Ligne> onboardingsParStatut, List<Ligne> tachesEnRetardParService,
            List<LigneMontant> masseSalarialeParDepartement, BigDecimal masseSalarialeTotale) {}

    private final EntityManager em;

    public ReportingService(EntityManager em) { this.em = em; }

    @Transactional(readOnly = true)
    public Synthese synthese() {
        var masse = masseSalarialeParDepartement();
        BigDecimal total = masse.stream().map(LigneMontant::montant).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Synthese(
                compter("select d.statutValidation, count(d) from DemandeRecrutement d group by d.statutValidation"),
                compter("select c.statutCandidature, count(c) from Candidature c group by c.statutCandidature"),
                compter("select p.statutProposition, count(p) from PropositionEmbauche p group by p.statutProposition"),
                compter("select e.statutActivite, count(e) from Employe e group by e.statutActivite"),
                compter("select a.poste.departement.libelle, count(distinct a.employe.id) from Affectation a "
                        + "where a.dateFin is null group by a.poste.departement.libelle order by a.poste.departement.libelle"),
                compter("select o.statutGlobal, count(o) from Onboarding o group by o.statutGlobal"),
                tachesEnRetard(),
                masse, total);
    }

    private List<Ligne> compter(String jpql) {
        List<Object[]> lignes = em.createQuery(jpql, Object[].class).getResultList();
        return lignes.stream().map(l -> new Ligne(String.valueOf(l[0]), ((Number) l[1]).longValue())).toList();
    }

    private List<Ligne> tachesEnRetard() {
        List<Object[]> lignes = em.createQuery("select t.serviceResponsable, count(t) from OnboardingTache t "
                + "where t.dateLimite < :aujourdhui and t.statutTache <> :validee group by t.serviceResponsable", Object[].class)
                .setParameter("aujourdhui", LocalDate.now())
                .setParameter("validee", TacheStatut.VALIDEE)
                .getResultList();
        return lignes.stream().map(l -> new Ligne(String.valueOf(l[0]), ((Number) l[1]).longValue())).toList();
    }

    /** Somme des salaires bruts des contrats en cours, par département du poste. */
    private List<LigneMontant> masseSalarialeParDepartement() {
        List<Object[]> lignes = em.createQuery("select c.poste.departement.libelle, sum(c.salaireBrut) from Contrat c "
                + "where c.statutContrat = :enCours group by c.poste.departement.libelle order by c.poste.departement.libelle",
                Object[].class).setParameter("enCours", ContratStatut.EN_COURS).getResultList();
        Map<String, BigDecimal> resultat = new LinkedHashMap<>();
        for (Object[] l : lignes) resultat.put(String.valueOf(l[0]), l[1] == null ? BigDecimal.ZERO : (BigDecimal) l[1]);
        return resultat.entrySet().stream().map(e -> new LigneMontant(e.getKey(), e.getValue())).toList();
    }
}
