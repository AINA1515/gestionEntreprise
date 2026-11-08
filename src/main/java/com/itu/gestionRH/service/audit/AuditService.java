package com.itu.gestionRH.service.audit;

import com.itu.gestionRH.entity.enums.AuditAction;
import com.itu.gestionRH.entity.security.JournalAudit;
import com.itu.gestionRH.repository.security.JournalAuditRepository;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.security.jwt.JwtAuthenticationFilter.JwtPrincipal;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Journal d'audit en ajout seul. Les modifications sont écrites dans la transaction métier (elles s'annulent avec elle) ;
 * les consultations dans une transaction à part, car elles s'exécutent souvent en lecture seule.
 */
@Service
public class AuditService {
    private static final int MAX_VALEUR = 2000;

    /** Une valeur modifiée. Pour un champ sensible, ne passer que des valeurs déjà masquées (sauf le salaire). */
    public record Changement(String champ, String ancienne, String nouvelle, boolean sensible) {}

    public record AuditDto(Long id, LocalDateTime dateEvenement, String login, Long utilisateurId, AuditAction action,
            String entite, Long entiteId, Long employeId, String champ, String ancienneValeur, String nouvelleValeur,
            boolean sensible, String motif, String operationId, String adresseIp) {}

    public record Filtre(Long employeId, String entite, Long entiteId, String login, AuditAction action, LocalDate du, LocalDate au) {}

    /** Construit la liste des seuls champs réellement modifiés. */
    public static final class Diff {
        private final List<Changement> changements = new ArrayList<>();
        public Diff champ(String nom, Object ancienne, Object nouvelle) {
            if (!Objects.equals(texte(ancienne), texte(nouvelle))) changements.add(new Changement(nom, texte(ancienne), texte(nouvelle), false));
            return this;
        }
        /** Valeurs déjà masquées par l'appelant (ex. « ****789 ») : seul le fait du changement est utile. */
        public Diff sensible(String nom, Object ancienneBrute, Object nouvelleBrute, java.util.function.Function<String, String> masque) {
            String a = texte(ancienneBrute), n = texte(nouvelleBrute);
            if (!Objects.equals(a, n)) changements.add(new Changement(nom, a == null ? null : masque.apply(a), n == null ? null : masque.apply(n), true));
            return this;
        }
        /** Montant : conservé en clair mais marqué sensible (masqué à l'affichage sans SALAIRE_CONSULTER). */
        public Diff montant(String nom, Object ancien, Object nouveau) {
            if (!Objects.equals(texte(ancien), texte(nouveau))) changements.add(new Changement(nom, texte(ancien), texte(nouveau), true));
            return this;
        }
        public List<Changement> liste() { return List.copyOf(changements); }
        public boolean vide() { return changements.isEmpty(); }
    }

    private final JournalAuditRepository journal;
    private final AccessScope accessScope;

    public AuditService(JournalAuditRepository journal, AccessScope accessScope) {
        this.journal = journal;
        this.accessScope = accessScope;
    }

    @Transactional
    public void tracer(AuditAction action, String entite, Long entiteId, Long employeId, List<Changement> changements, String motif) {
        if (changements == null || changements.isEmpty()) {
            if (action == AuditAction.MODIFICATION) return;
            ecrire(action, entite, entiteId, employeId, null, motif, UUID.randomUUID().toString());
            return;
        }
        String operation = UUID.randomUUID().toString();
        for (Changement c : changements) ecrire(action, entite, entiteId, employeId, c, motif, operation);
    }

    /** Événement sans détail de champ (création, validation, suppression…). */
    @Transactional
    public void tracer(AuditAction action, String entite, Long entiteId, Long employeId, String motif) {
        ecrire(action, entite, entiteId, employeId, null, motif, UUID.randomUUID().toString());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void tracerConsultation(String entite, Long entiteId, Long employeId, String motif) {
        ecrire(AuditAction.CONSULTATION, entite, entiteId, employeId, null, motif, UUID.randomUUID().toString());
    }

    @Transactional(readOnly = true)
    public Page<AuditDto> rechercher(Filtre f, Pageable pageable) {
        boolean voirSensibles = accessScope.hasPermission("DONNEES_SENSIBLES_CONSULTER") || accessScope.hasPermission("SALAIRE_CONSULTER");
        Specification<JournalAudit> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f.employeId() != null) p.add(cb.equal(root.get("employeId"), f.employeId()));
            if (f.entite() != null && !f.entite().isBlank()) p.add(cb.equal(root.get("entite"), f.entite().trim().toUpperCase()));
            if (f.entiteId() != null) p.add(cb.equal(root.get("entiteId"), f.entiteId()));
            if (f.login() != null && !f.login().isBlank()) p.add(cb.like(cb.lower(root.get("login")), "%" + f.login().trim().toLowerCase() + "%"));
            if (f.action() != null) p.add(cb.equal(root.get("action"), f.action()));
            if (f.du() != null) p.add(cb.greaterThanOrEqualTo(root.get("dateEvenement"), f.du().atStartOfDay()));
            if (f.au() != null) p.add(cb.lessThan(root.get("dateEvenement"), f.au().plusDays(1).atStartOfDay()));
            return cb.and(p.toArray(new Predicate[0]));
        };
        return journal.findAll(spec, pageable).map(j -> dto(j, voirSensibles));
    }

    private AuditDto dto(JournalAudit j, boolean voirSensibles) {
        boolean masquer = Boolean.TRUE.equals(j.getEstSensible()) && !voirSensibles;
        return new AuditDto(j.getId(), j.getDateEvenement(), j.getLogin(), j.getUtilisateurId(), j.getAction(), j.getEntite(),
                j.getEntiteId(), j.getEmployeId(), j.getChamp(), masquer && j.getAncienneValeur() != null ? "****" : j.getAncienneValeur(),
                masquer && j.getNouvelleValeur() != null ? "****" : j.getNouvelleValeur(), Boolean.TRUE.equals(j.getEstSensible()),
                j.getMotif(), j.getOperationId(), j.getAdresseIp());
    }

    private void ecrire(AuditAction action, String entite, Long entiteId, Long employeId, Changement c, String motif, String operation) {
        JournalAudit j = JournalAudit.nouvelle();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean connecte = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
        j.setLogin(connecte ? limiter(auth.getName(), 100) : "SYSTEME");
        if (connecte && auth.getPrincipal() instanceof JwtPrincipal p) j.setUtilisateurId(p.userId());
        j.setDateEvenement(LocalDateTime.now());
        j.setAction(action);
        j.setEntite(entite);
        j.setEntiteId(entiteId);
        j.setEmployeId(employeId);
        if (c != null) {
            j.setChamp(limiter(c.champ(), 60));
            j.setAncienneValeur(limiter(c.ancienne(), MAX_VALEUR));
            j.setNouvelleValeur(limiter(c.nouvelle(), MAX_VALEUR));
        }
        j.setEstSensible(c != null && c.sensible());
        j.setMotif(limiter(motif, 500));
        j.setOperationId(operation);
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)
            j.setAdresseIp(limiter(attrs.getRequest().getRemoteAddr(), 45));
        journal.save(j);
    }

    private static String texte(Object o) { return o == null ? null : o.toString(); }
    private static String limiter(String s, int max) { return s == null ? null : s.length() <= max ? s : s.substring(0, max); }
}
