package com.itu.gestionRH.service.organization;

import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.controller.organization.DossierDtos.*;
import com.itu.gestionRH.controller.organization.OrganisationDtos.AffectationResume;
import com.itu.gestionRH.controller.organization.OrganisationDtos.ContratResume;
import com.itu.gestionRH.entity.enums.ContratActe;
import com.itu.gestionRH.entity.enums.ContratStatut;
import com.itu.gestionRH.entity.enums.EmployeStatut;
import com.itu.gestionRH.entity.hiring.Affectation;
import com.itu.gestionRH.entity.hiring.Contrat;
import com.itu.gestionRH.entity.hiring.EvolutionSalaire;
import com.itu.gestionRH.entity.organization.Employe;
import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.repository.hiring.ContratRepository;
import com.itu.gestionRH.repository.hiring.EvolutionSalaireRepository;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.security.AccessScope;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dossier administratif du salarié : lecture avec masquage des champs sensibles, correction par la RH,
 * mise à jour des coordonnées par le salarié, chronologie de carrière et historique de rémunération.
 */
@Service
@Transactional(readOnly = true)
public class DossierService {
    private static final List<ContratStatut> EN_COURS = List.of(ContratStatut.EN_COURS);
    private static final String ACCES_REFUSE = "Accès refusé.";

    private final EmployeRepository employes;
    private final AffectationRepository affectations;
    private final ContratRepository contrats;
    private final EvolutionSalaireRepository evolutions;
    private final UtilisateurRepository utilisateurs;
    private final AccessScope accessScope;

    public DossierService(EmployeRepository employes, AffectationRepository affectations, ContratRepository contrats,
            EvolutionSalaireRepository evolutions, UtilisateurRepository utilisateurs, AccessScope accessScope) {
        this.employes = employes;
        this.affectations = affectations;
        this.contrats = contrats;
        this.evolutions = evolutions;
        this.utilisateurs = utilisateurs;
        this.accessScope = accessScope;
    }

    // ------------------------------------------------------------------ lecture

    public DossierDto obtenir(Long id) {
        accessScope.requireDossier(id);
        return dto(trouver(id));
    }

    public Long employeIdDe(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurs.findById(utilisateurId)
                .orElseThrow(() -> new NotFoundException("Dossier salarié introuvable."));
        if (utilisateur.getEmployeId() == null) throw new NotFoundException("Dossier salarié introuvable.");
        return utilisateur.getEmployeId();
    }

    public DossierDto moi(Long utilisateurId) { return obtenir(employeIdDe(utilisateurId)); }

    // ------------------------------------------------------------------ modification

    /**
     * Correction du dossier par la RH. Les blocs absents (null) sont laissés tels quels. Les champs sensibles ne sont
     * pris en compte qu'avec DONNEES_SENSIBLES_CONSULTER, et une valeur masquée (« ****123 ») n'écrase jamais l'existant.
     */
    @Transactional
    public DossierDto modifierParRh(Long id, DossierRhRequest r) {
        Employe e = trouver(id);
        verifierVersion(e, r.version());
        boolean sensibles = accessScope.hasPermission("DONNEES_SENSIBLES_CONSULTER");
        Identite i = r.identite();
        Coordonnees c = r.coordonnees();

        String email = nettoyer(c.emailProfessionnel());
        if (email != null && !email.equalsIgnoreCase(e.getEmailProfessionnel())
                && employes.compterParEmailProfessionnel(email.toLowerCase(Locale.ROOT), id) > 0)
            throw new BusinessRuleException("EMAIL_PROFESSIONNEL_DEJA_UTILISE", "Cet e-mail professionnel est déjà attribué à un autre salarié.");

        String cin = sensibles ? valeurSensible(e.getNumCin(), i.numCin()) : e.getNumCin();
        String cnaps = sensibles ? valeurSensible(e.getNumCnaps(), i.numCnaps()) : e.getNumCnaps();
        if (cin != null && !cin.equals(e.getNumCin()) && employes.compterParCin(normaliser(cin), id) > 0)
            throw new BusinessRuleException("CIN_DEJA_UTILISE", "Ce numéro de CIN est déjà enregistré pour un autre salarié.");
        if (cnaps != null && !cnaps.equals(e.getNumCnaps()) && employes.compterParCnaps(normaliser(cnaps), id) > 0)
            throw new BusinessRuleException("CNAPS_DEJA_UTILISE", "Ce numéro CNaPS est déjà enregistré pour un autre salarié.");

        e.setCivilite(nettoyer(i.civilite()));
        e.setNom(i.nom().trim());
        e.setPrenom(i.prenom().trim());
        e.setDateNaissance(i.dateNaissance());
        e.setLieuNaissance(nettoyer(i.lieuNaissance()));
        e.setNationalite(nettoyer(i.nationalite()));
        e.setSexe(nettoyer(i.sexe()));
        e.setSituationFamiliale(i.situationFamiliale());
        e.setNumCin(cin);
        e.setNumCnaps(cnaps);

        e.setEmailProfessionnel(email);
        e.setTelephone(nettoyer(c.telephone()));
        appliquerCoordonneesPersonnelles(e, c.emailPersonnel(), c.telephonePersonnel(), c.adresseLigne1(),
                c.adresseLigne2(), c.codePostal(), c.ville(), c.pays());

        if (r.contactUrgence() != null) appliquerUrgence(e, r.contactUrgence());
        if (sensibles && r.coordonneesBancaires() != null) appliquerBanque(e, r.coordonneesBancaires());

        employes.saveAndFlush(e);
        return dto(e);
    }

    /** Mise à jour par le salarié de ses coordonnées et de son contact d'urgence (rien d'autre). */
    @Transactional
    public DossierDto modifierCoordonnees(Long utilisateurId, CoordonneesPersonnellesRequest r) {
        Employe e = trouver(employeIdDe(utilisateurId));
        verifierVersion(e, r.version());
        appliquerCoordonneesPersonnelles(e, r.emailPersonnel(), r.telephonePersonnel(), r.adresseLigne1(),
                r.adresseLigne2(), r.codePostal(), r.ville(), r.pays());
        if (r.contactUrgence() != null) appliquerUrgence(e, r.contactUrgence());
        employes.saveAndFlush(e);
        return dto(e);
    }

    // ------------------------------------------------------------------ carrière

    /** Chronologie unique : embauche, contrats, avenants, mutations, changements de salaire, fin de contrat, sortie. */
    public List<CarriereEvenement> carriere(Long id) {
        accessScope.requireDossier(id);
        Employe e = trouver(id);
        boolean salaire = accessScope.peutVoirSalaire(id);
        List<CarriereEvenement> evenements = new ArrayList<>();

        evenements.add(new CarriereEvenement(e.getDateEmbaucheInitiale(), CarriereEvenementType.EMBAUCHE,
                "Embauche", "Matricule " + e.getMatricule(), "EMPLOYE", e.getId(), null, null, null));

        List<Contrat> contratsAsc = new ArrayList<>(contrats.findByEmployeIdOrderByDateDebutDescIdDesc(id));
        Collections.reverse(contratsAsc);
        for (Contrat c : contratsAsc) {
            CarriereEvenementType type = switch (c.getTypeActe()) {
                case INITIAL -> CarriereEvenementType.CONTRAT;
                case AVENANT -> CarriereEvenementType.AVENANT;
                case RENOUVELLEMENT -> CarriereEvenementType.RENOUVELLEMENT;
            };
            String libelle = switch (c.getTypeActe()) {
                case INITIAL -> "Contrat " + c.getTypeContrat().name();
                case AVENANT -> "Avenant au contrat " + c.getTypeContrat().name();
                case RENOUVELLEMENT -> "Renouvellement du contrat " + c.getTypeContrat().name();
            };
            String detail = "Poste : " + c.getPoste().getIntitule()
                    + (c.getDateFin() == null ? "" : " — jusqu'au " + c.getDateFin());
            BigDecimal ancien = c.getContratPrecedent() == null ? null : c.getContratPrecedent().getSalaireBrut();
            evenements.add(new CarriereEvenement(c.getDateDebut(), type, libelle, detail, "CONTRAT", c.getId(),
                    salaire ? ancien : null, salaire ? c.getSalaireBrut() : null, salaire ? c.getDevise() : null));
            if (c.getStatutContrat() == ContratStatut.TERMINE && c.getDateFinEffective() != null)
                evenements.add(new CarriereEvenement(c.getDateFinEffective(), CarriereEvenementType.FIN_CONTRAT,
                        "Fin du contrat " + c.getTypeContrat().name(), detail, "CONTRAT", c.getId(), null, null, null));
        }

        List<Affectation> affectationsAsc = new ArrayList<>(affectations.findByEmployeIdOrderByDateDebutDescIdDesc(id));
        Collections.reverse(affectationsAsc);
        Long postePrecedent = null;
        for (Affectation a : affectationsAsc) {
            Long posteId = a.getPoste().getId();
            if (posteId != null && posteId.equals(postePrecedent)) continue;
            CarriereEvenementType type = postePrecedent == null ? CarriereEvenementType.AFFECTATION : CarriereEvenementType.MUTATION;
            String detail = a.getPoste().getIntitule() + " — " + a.getPoste().getDepartement().getLibelle()
                    + (a.getMotif() == null ? "" : " (" + a.getMotif() + ")");
            evenements.add(new CarriereEvenement(a.getDateDebut(), type,
                    type == CarriereEvenementType.AFFECTATION ? "Affectation" : "Mutation", detail,
                    "AFFECTATION", a.getId(), null, null, null));
            postePrecedent = posteId;
        }

        for (EvolutionSalaire ev : evolutions.findByEmployeIdOrderByDateEffetAscIdAsc(id)) {
            evenements.add(new CarriereEvenement(ev.getDateEffet(), CarriereEvenementType.CHANGEMENT_SALAIRE,
                    "Changement de salaire (" + ev.getTypeEvolution().name().toLowerCase(Locale.ROOT) + ")", ev.getMotif(),
                    "EVOLUTION_SALAIRE", ev.getId(), salaire ? ev.getSalaireBrutAncien() : null,
                    salaire ? ev.getSalaireBrutNouveau() : null, salaire ? ev.getDevise() : null));
        }

        if (e.getDateSortie() != null)
            evenements.add(new CarriereEvenement(e.getDateSortie(), CarriereEvenementType.SORTIE, "Sortie des effectifs",
                    e.getMotifSortie(), "EMPLOYE", e.getId(), null, null, null));

        evenements.sort(Comparator.comparing(CarriereEvenement::date)
                .thenComparing(ev -> ev.type().ordinal())
                .thenComparing(CarriereEvenement::sourceId, Comparator.nullsLast(Comparator.naturalOrder())));
        return evenements;
    }

    // ------------------------------------------------------------------ rémunération

    public RemunerationDto remuneration(Long id) {
        accessScope.requireDossier(id);
        trouver(id);
        exigerSalaire(id);
        List<PointSalaire> historique = points(id);
        LocalDate aujourdHui = LocalDate.now();
        PointSalaire actuel = pointA(historique, aujourdHui);
        return new RemunerationDto(id, actuel == null ? null : actuel.salaireNouveau(),
                actuel == null ? null : actuel.devise(), actuel == null ? null : actuel.dateEffet(), historique);
    }

    /** Enregistre une augmentation ou correction sans avenant ; le contrat en cours reste inchangé. */
    @Transactional
    public PointSalaire enregistrerEvolution(Long id, EvolutionSalaireRequest r) {
        Employe e = trouver(id);
        exigerSalaire(id);
        if (e.getStatutActivite() == EmployeStatut.SORTI)
            throw new BusinessRuleException("SALARIE_SORTI", "Le salarié est sorti : aucun changement de salaire possible.");
        Contrat contrat = contrats.findFirstByEmployeIdAndStatutContratInOrderByDateDebutDescIdDesc(id, EN_COURS)
                .orElseThrow(() -> new BusinessRuleException("AUCUN_CONTRAT_EN_COURS", "Le salarié n'a aucun contrat en cours."));
        if (r.dateEffet().isBefore(contrat.getDateDebut()))
            throw new BusinessRuleException("DATE_EFFET_INVALIDE", "La date d'effet précède le début du contrat en cours.", HttpStatus.BAD_REQUEST);
        if (contrat.getDateFin() != null && r.dateEffet().isAfter(contrat.getDateFin()))
            throw new BusinessRuleException("DATE_EFFET_INVALIDE", "La date d'effet dépasse la fin du contrat en cours.", HttpStatus.BAD_REQUEST);
        if (evolutions.findByEmployeIdOrderByDateEffetAscIdAsc(id).stream().anyMatch(ev -> ev.getDateEffet().equals(r.dateEffet())))
            throw new BusinessRuleException("EVOLUTION_DEJA_ENREGISTREE", "Un changement de salaire existe déjà à cette date.");

        PointSalaire avant = pointA(points(id), r.dateEffet().minusDays(1));
        BigDecimal ancien = avant != null ? avant.salaireNouveau() : contrat.getSalaireBrut();
        BigDecimal nouveau = r.salaireBrutNouveau().setScale(2, RoundingMode.HALF_UP);
        if (ancien != null && ancien.compareTo(nouveau) == 0)
            throw new BusinessRuleException("SALAIRE_INCHANGE", "Le nouveau salaire est identique au salaire en vigueur.", HttpStatus.BAD_REQUEST);

        EvolutionSalaire ev = EvolutionSalaire.nouvelle();
        ev.setEmploye(e);
        ev.setContrat(contrat);
        ev.setDateEffet(r.dateEffet());
        ev.setSalaireBrutAncien(ancien);
        ev.setSalaireBrutNouveau(nouveau);
        ev.setDevise(contrat.getDevise());
        ev.setTypeEvolution(r.typeEvolution());
        ev.setMotif(r.motif().trim());
        evolutions.saveAndFlush(ev);
        return new PointSalaire(ev.getDateEffet(), ancien, nouveau, ev.getDevise(), "EVOLUTION",
                ev.getTypeEvolution().name(), ev.getMotif(), contrat.getId(), ev.getId());
    }

    // ------------------------------------------------------------------ internes

    private record Entree(LocalDate date, int ordre, long id, PointSalaire point) {}

    /** Contrats (hors avenants sans changement de salaire) et évolutions, du plus ancien au plus récent. */
    private List<PointSalaire> points(Long employeId) {
        List<Entree> entrees = new ArrayList<>();
        for (Contrat c : contrats.findByEmployeIdOrderByDateDebutDescIdDesc(employeId)) {
            BigDecimal ancien = c.getContratPrecedent() == null ? null : c.getContratPrecedent().getSalaireBrut();
            if (ancien != null && ancien.compareTo(c.getSalaireBrut()) == 0) continue;
            String origine = c.getTypeActe() == ContratActe.INITIAL ? "CONTRAT_INITIAL" : c.getTypeActe().name();
            entrees.add(new Entree(c.getDateDebut(), 0, c.getId(), new PointSalaire(c.getDateDebut(), ancien,
                    c.getSalaireBrut(), c.getDevise(), origine, null, null, c.getId(), null)));
        }
        for (EvolutionSalaire ev : evolutions.findByEmployeIdOrderByDateEffetAscIdAsc(employeId)) {
            entrees.add(new Entree(ev.getDateEffet(), 1, ev.getId(), new PointSalaire(ev.getDateEffet(),
                    ev.getSalaireBrutAncien(), ev.getSalaireBrutNouveau(), ev.getDevise(), "EVOLUTION",
                    ev.getTypeEvolution().name(), ev.getMotif(), ev.getContrat() == null ? null : ev.getContrat().getId(), ev.getId())));
        }
        entrees.sort(Comparator.comparing(Entree::date).thenComparingInt(Entree::ordre).thenComparingLong(Entree::id));
        return entrees.stream().map(Entree::point).toList();
    }

    /** Dernier point en vigueur à la date donnée (liste triée par date croissante). */
    private static PointSalaire pointA(List<PointSalaire> points, LocalDate date) {
        PointSalaire retenu = null;
        for (PointSalaire p : points) if (!p.dateEffet().isAfter(date)) retenu = p;
        return retenu;
    }

    private void exigerSalaire(Long employeId) {
        if (!accessScope.peutVoirSalaire(employeId)) throw new AccessDeniedException(ACCES_REFUSE);
    }

    private Employe trouver(Long id) {
        return employes.findById(id).orElseThrow(() -> new NotFoundException("Dossier salarié introuvable."));
    }

    private void verifierVersion(Employe e, Long versionRecue) {
        if (versionRecue != null && e.getVersionLigne() != null && !versionRecue.equals(e.getVersionLigne()))
            throw new BusinessRuleException("DOSSIER_MODIFIE",
                    "Le dossier a été modifié entre-temps. Rechargez-le avant de réenregistrer.");
    }

    private void appliquerCoordonneesPersonnelles(Employe e, String email, String telephone, String ligne1,
            String ligne2, String codePostal, String ville, String pays) {
        e.setEmailPersonnel(nettoyer(email));
        e.setTelephonePersonnel(nettoyer(telephone));
        e.setAdresseLigne1(nettoyer(ligne1));
        e.setAdresseLigne2(nettoyer(ligne2));
        e.setCodePostal(nettoyer(codePostal));
        e.setVille(nettoyer(ville));
        e.setPays(nettoyer(pays));
    }

    private void appliquerUrgence(Employe e, ContactUrgence u) {
        e.setUrgenceNom(nettoyer(u.nom()));
        e.setUrgenceLien(nettoyer(u.lien()));
        e.setUrgenceTelephone(nettoyer(u.telephone()));
    }

    private void appliquerBanque(Employe e, CoordonneesBancaires b) {
        e.setBanqueNom(nettoyer(b.banqueNom()));
        e.setTitulaireCompte(nettoyer(b.titulaire()));
        e.setRibCodeBanque(formatValide(valeurSensible(e.getRibCodeBanque(), b.codeBanque()), e.getRibCodeBanque(), "^[0-9]{5}$", "Le code banque doit comporter 5 chiffres."));
        e.setRibCodeGuichet(formatValide(valeurSensible(e.getRibCodeGuichet(), b.codeGuichet()), e.getRibCodeGuichet(), "^[0-9]{5}$", "Le code guichet doit comporter 5 chiffres."));
        e.setRibNumeroCompte(formatValide(valeurSensible(e.getRibNumeroCompte(), b.numeroCompte()), e.getRibNumeroCompte(), "^[0-9A-Za-z]{11}$", "Le numéro de compte doit comporter 11 caractères."));
        e.setRibCle(formatValide(valeurSensible(e.getRibCle(), b.cle()), e.getRibCle(), "^[0-9]{2}$", "La clé RIB doit comporter 2 chiffres."));
        String iban = valeurSensible(e.getIban(), b.iban());
        if (iban != null) iban = normaliser(iban);
        e.setIban(formatValide(iban, e.getIban(), "^[A-Z]{2}[0-9A-Z]{12,32}$", "L'IBAN est invalide."));
    }

    /** Ne contrôle le format que si la valeur change, pour ne pas bloquer la correction d'autres champs. */
    private static String formatValide(String nouvelle, String ancienne, String regex, String message) {
        if (nouvelle == null || nouvelle.equals(ancienne)) return nouvelle;
        if (!nouvelle.matches(regex)) throw new BusinessRuleException("COORDONNEES_BANCAIRES_INVALIDES", message, HttpStatus.BAD_REQUEST);
        return nouvelle;
    }

    /** Une valeur masquée renvoyée telle quelle par l'écran ne remplace jamais la valeur réelle. */
    private static String valeurSensible(String ancienne, String recue) {
        if (recue != null && recue.contains("*")) return ancienne;
        return nettoyer(recue);
    }

    private static String nettoyer(String valeur) {
        if (valeur == null) return null;
        String v = valeur.trim();
        return v.isEmpty() ? null : v;
    }

    private static String normaliser(String valeur) { return valeur.replace(" ", "").toUpperCase(Locale.ROOT); }

    /** Garde les 3 derniers caractères : « ****123 ». */
    static String masquer(String valeur) {
        if (valeur == null || valeur.isBlank()) return null;
        String v = valeur.trim();
        return v.length() <= 4 ? "****" : "****" + v.substring(v.length() - 3);
    }

    private DossierDto dto(Employe e) {
        Long id = e.getId();
        boolean sensibles = accessScope.peutVoirDonneesSensibles(id);
        boolean salaire = accessScope.peutVoirSalaire(id);
        Affectation courante = affectations.findFirstByEmployeIdAndDateFinIsNullOrderByDateDebutDesc(id).orElse(null);
        AffectationResume affectation = null;
        Long departementId = null;
        String departement = null;
        if (courante != null) {
            var poste = courante.getPoste();
            departementId = poste.getDepartement().getId();
            departement = poste.getDepartement().getLibelle();
            affectation = new AffectationResume(poste.getId(), poste.getCodePoste(), poste.getIntitule(),
                    poste.getStatutPoste().name(), courante.getDateDebut(),
                    courante.getSuperieur() == null ? null : courante.getSuperieur().getId());
        }
        ContratResume contrat = contrats.findFirstByEmployeIdAndStatutContratInOrderByDateDebutDescIdDesc(id, EN_COURS)
                .map(c -> new ContratResume(c.getId(), c.getTypeContrat().name(), c.getTypeActe().name(),
                        c.getContratPrecedent() == null ? null : c.getContratPrecedent().getId(), c.getDateDebut(),
                        c.getDateFin(), c.getDateFinEffective(), salaire ? c.getSalaireBrut() : null, c.getDevise(),
                        c.getStatutContrat().name()))
                .orElse(null);

        Identite identite = new Identite(e.getCivilite(), e.getNom(), e.getPrenom(), e.getDateNaissance(),
                e.getLieuNaissance(), e.getNationalite(), e.getSexe(), e.getSituationFamiliale(),
                sensibles ? e.getNumCin() : masquer(e.getNumCin()), sensibles ? e.getNumCnaps() : masquer(e.getNumCnaps()));
        Coordonnees coordonnees = new Coordonnees(e.getEmailProfessionnel(), e.getTelephone(), e.getEmailPersonnel(),
                e.getTelephonePersonnel(), e.getAdresseLigne1(), e.getAdresseLigne2(), e.getCodePostal(), e.getVille(), e.getPays());
        ContactUrgence urgence = new ContactUrgence(e.getUrgenceNom(), e.getUrgenceLien(), e.getUrgenceTelephone());
        CoordonneesBancaires banque = sensibles
                ? new CoordonneesBancaires(e.getBanqueNom(), e.getRibCodeBanque(), e.getRibCodeGuichet(),
                        e.getRibNumeroCompte(), e.getRibCle(), e.getIban(), e.getTitulaireCompte())
                : new CoordonneesBancaires(e.getBanqueNom(), masquer(e.getRibCodeBanque()), masquer(e.getRibCodeGuichet()),
                        masquer(e.getRibNumeroCompte()), masquer(e.getRibCle()), masquer(e.getIban()), e.getTitulaireCompte());
        Droits droits = new Droits(sensibles, salaire, accessScope.hasPermission("DOSSIER_MODIFIER"),
                accessScope.hasPermission("DOSSIER_MODIFIER_PERSO") && accessScope.estSoiMeme(id));
        return new DossierDto(id, e.getMatricule(), e.getStatutActivite(), e.getDateEmbaucheInitiale(), e.getDateSortie(),
                e.getMotifSortie(), e.getVersionLigne(), identite, coordonnees, urgence, banque, departementId,
                departement, affectation, contrat, droits);
    }
}
