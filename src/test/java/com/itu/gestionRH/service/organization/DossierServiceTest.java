package com.itu.gestionRH.service.organization;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.controller.organization.DossierDtos.*;
import com.itu.gestionRH.entity.enums.ContratActe;
import com.itu.gestionRH.entity.enums.ContratStatut;
import com.itu.gestionRH.entity.enums.ContratType;
import com.itu.gestionRH.entity.enums.EmployeStatut;
import com.itu.gestionRH.entity.enums.EvolutionSalaireType;
import com.itu.gestionRH.entity.hiring.Affectation;
import com.itu.gestionRH.entity.hiring.Contrat;
import com.itu.gestionRH.entity.hiring.EvolutionSalaire;
import com.itu.gestionRH.entity.organization.Departement;
import com.itu.gestionRH.entity.organization.Employe;
import com.itu.gestionRH.entity.organization.Poste;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.repository.hiring.ContratRepository;
import com.itu.gestionRH.repository.hiring.EvolutionSalaireRepository;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.security.AccessScope;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

class DossierServiceTest {
    private EmployeRepository employes;
    private AffectationRepository affectations;
    private ContratRepository contrats;
    private EvolutionSalaireRepository evolutions;
    private AccessScope scope;
    private DossierService service;
    private Employe employe;

    @BeforeEach
    void init() {
        employes = mock(EmployeRepository.class);
        affectations = mock(AffectationRepository.class);
        contrats = mock(ContratRepository.class);
        evolutions = mock(EvolutionSalaireRepository.class);
        scope = mock(AccessScope.class);
        service = new DossierService(employes, affectations, contrats, evolutions, mock(UtilisateurRepository.class), scope);

        employe = Employe.nouvelle();
        ReflectionTestUtils.setField(employe, "id", 5L);
        ReflectionTestUtils.setField(employe, "versionLigne", 3L);
        employe.setMatricule("EMP-2024-0001");
        employe.setNom("Rakoto");
        employe.setPrenom("Jean");
        employe.setDateEmbaucheInitiale(LocalDate.of(2024, 1, 1));
        employe.setStatutActivite(EmployeStatut.ACTIF);
        employe.setNumCin("101 231 456 789");
        employe.setNumCnaps("CN123456");
        employe.setIban("MG4600005030010101914");
        employe.setRibNumeroCompte("01019140000");
        when(employes.findById(5L)).thenReturn(Optional.of(employe));
        when(employes.saveAndFlush(any(Employe.class))).thenAnswer(i -> i.getArgument(0));
    }

    private static Identite identite(String cin, String cnaps) {
        return new Identite("M.", "Rakoto", "Jean", LocalDate.of(1990, 5, 1), "Antananarivo", "Malagasy", "M", null, cin, cnaps);
    }

    private static DossierRhRequest requete(Long version, Identite identite, CoordonneesBancaires banque) {
        return new DossierRhRequest(version, identite,
                new Coordonnees("jean@demo-rh.local", "0340000000", null, null, null, null, null, null, null), null, banque);
    }

    @Test
    void sansHabilitationLesChampsSensiblesSontMasques() {
        when(scope.peutVoirDonneesSensibles(5L)).thenReturn(false);
        DossierDto dossier = service.obtenir(5L);
        assertEquals("****789", dossier.identite().numCin());
        assertEquals("****456", dossier.identite().numCnaps());
        assertTrue(dossier.coordonneesBancaires().iban().startsWith("****"));
        assertTrue(dossier.coordonneesBancaires().iban().endsWith("914"));
        assertFalse(dossier.coordonneesBancaires().iban().contains("MG46"));
        assertTrue(dossier.coordonneesBancaires().numeroCompte().startsWith("****"));
        assertFalse(dossier.droits().donneesSensiblesVisibles());
    }

    @Test
    void avecHabilitationLesChampsSensiblesSontEnClair() {
        when(scope.peutVoirDonneesSensibles(5L)).thenReturn(true);
        DossierDto dossier = service.obtenir(5L);
        assertEquals("101 231 456 789", dossier.identite().numCin());
        assertEquals("MG4600005030010101914", dossier.coordonneesBancaires().iban());
    }

    @Test
    void salaireDuContratCourantMasqueSansDroit() {
        Contrat c = contrat(1L, ContratActe.INITIAL, ContratStatut.EN_COURS, "1500.00", LocalDate.of(2024, 1, 1), null);
        when(contrats.findFirstByEmployeIdAndStatutContratInOrderByDateDebutDescIdDesc(eq(5L), any())).thenReturn(Optional.of(c));
        when(scope.peutVoirSalaire(5L)).thenReturn(false);
        assertNull(service.obtenir(5L).contratCourant().salaireBrut());
        when(scope.peutVoirSalaire(5L)).thenReturn(true);
        assertEquals(new BigDecimal("1500.00"), service.obtenir(5L).contratCourant().salaireBrut());
    }

    @Test
    void sansHabilitationSensibleLaRhNeModifiePasCinNiBanque() {
        when(scope.hasPermission("DONNEES_SENSIBLES_CONSULTER")).thenReturn(false);
        service.modifierParRh(5L, requete(3L, identite("999", "XX"), new CoordonneesBancaires("BOA", "00005", "03001", "01019140000", "99", "MG0000000000000", "Jean")));
        assertEquals("101 231 456 789", employe.getNumCin());
        assertEquals("CN123456", employe.getNumCnaps());
        assertEquals("MG4600005030010101914", employe.getIban());
        assertNull(employe.getBanqueNom());
    }

    @Test
    void uneValeurMasqueeNEcraseJamaisLaValeurReelle() {
        when(scope.hasPermission("DONNEES_SENSIBLES_CONSULTER")).thenReturn(true);
        service.modifierParRh(5L, requete(3L, identite("****789", "****456"),
                new CoordonneesBancaires("BOA", "****", "****", "****914", "****", "****914", "Jean")));
        assertEquals("101 231 456 789", employe.getNumCin());
        assertEquals("CN123456", employe.getNumCnaps());
        assertEquals("MG4600005030010101914", employe.getIban());
        assertEquals("BOA", employe.getBanqueNom());
    }

    @Test
    void conflitDeVersionRefuse() {
        var ex = assertThrows(BusinessRuleException.class, () -> service.modifierParRh(5L, requete(2L, identite(null, null), null)));
        assertEquals("DOSSIER_MODIFIE", ex.getCode());
        verify(employes, never()).saveAndFlush(any());
    }

    @Test
    void cinDejaUtiliseRefuse() {
        when(scope.hasPermission("DONNEES_SENSIBLES_CONSULTER")).thenReturn(true);
        when(employes.compterParCin("202", 5L)).thenReturn(1L);
        var ex = assertThrows(BusinessRuleException.class, () -> service.modifierParRh(5L, requete(3L, identite("202", null), null)));
        assertEquals("CIN_DEJA_UTILISE", ex.getCode());
    }

    @Test
    void formatRibInvalideRefuse() {
        when(scope.hasPermission("DONNEES_SENSIBLES_CONSULTER")).thenReturn(true);
        var banque = new CoordonneesBancaires("BOA", "12", null, null, null, null, null);
        var ex = assertThrows(BusinessRuleException.class, () -> service.modifierParRh(5L, requete(3L, identite("101 231 456 789", "CN123456"), banque)));
        assertEquals("COORDONNEES_BANCAIRES_INVALIDES", ex.getCode());
    }

    @Test
    void carriereEstChronologiqueEtMasqueLesMontantsSansDroit() {
        Contrat initial = contrat(10L, ContratActe.INITIAL, ContratStatut.TERMINE, "1000.00", LocalDate.of(2024, 1, 1), null);
        initial.setDateFinEffective(LocalDate.of(2024, 12, 31));
        Affectation aff = Affectation.nouvelle();
        ReflectionTestUtils.setField(aff, "id", 20L);
        aff.setEmploye(employe);
        aff.setPoste(initial.getPoste());
        aff.setDateDebut(LocalDate.of(2024, 1, 1));
        EvolutionSalaire ev = EvolutionSalaire.nouvelle();
        ReflectionTestUtils.setField(ev, "id", 30L);
        ev.setDateEffet(LocalDate.of(2024, 7, 1));
        ev.setSalaireBrutAncien(new BigDecimal("1000.00"));
        ev.setSalaireBrutNouveau(new BigDecimal("1200.00"));
        ev.setDevise("MGA");
        ev.setTypeEvolution(EvolutionSalaireType.AUGMENTATION);
        ev.setMotif("Revalorisation annuelle");
        employe.setDateSortie(LocalDate.of(2024, 12, 31));
        employe.setMotifSortie("Fin de contrat");
        when(contrats.findByEmployeIdOrderByDateDebutDescIdDesc(5L)).thenReturn(List.of(initial));
        when(affectations.findByEmployeIdOrderByDateDebutDescIdDesc(5L)).thenReturn(List.of(aff));
        when(evolutions.findByEmployeIdOrderByDateEffetAscIdAsc(5L)).thenReturn(List.of(ev));

        when(scope.peutVoirSalaire(5L)).thenReturn(false);
        var sans = service.carriere(5L);
        assertEquals(List.of(CarriereEvenementType.EMBAUCHE, CarriereEvenementType.CONTRAT, CarriereEvenementType.AFFECTATION,
                CarriereEvenementType.CHANGEMENT_SALAIRE, CarriereEvenementType.FIN_CONTRAT, CarriereEvenementType.SORTIE),
                sans.stream().map(CarriereEvenement::type).toList());
        assertTrue(sans.stream().allMatch(x -> x.salaireNouveau() == null && x.salaireAncien() == null));

        when(scope.peutVoirSalaire(5L)).thenReturn(true);
        var avec = service.carriere(5L);
        assertEquals(new BigDecimal("1200.00"), avec.stream().filter(x -> x.type() == CarriereEvenementType.CHANGEMENT_SALAIRE).findFirst().orElseThrow().salaireNouveau());
    }

    @Test
    void remunerationRefuseeSansHabilitationSalaire() {
        when(scope.peutVoirSalaire(5L)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.remuneration(5L));
    }

    @Test
    void remunerationCombineContratsEtEvolutions() {
        Contrat initial = contrat(10L, ContratActe.INITIAL, ContratStatut.EN_COURS, "1000.00", LocalDate.of(2024, 1, 1), null);
        EvolutionSalaire ev = EvolutionSalaire.nouvelle();
        ReflectionTestUtils.setField(ev, "id", 30L);
        ev.setEmploye(employe);
        ev.setContrat(initial);
        ev.setDateEffet(LocalDate.of(2024, 7, 1));
        ev.setSalaireBrutAncien(new BigDecimal("1000.00"));
        ev.setSalaireBrutNouveau(new BigDecimal("1200.00"));
        ev.setDevise("MGA");
        ev.setTypeEvolution(EvolutionSalaireType.AUGMENTATION);
        when(scope.peutVoirSalaire(5L)).thenReturn(true);
        when(contrats.findByEmployeIdOrderByDateDebutDescIdDesc(5L)).thenReturn(List.of(initial));
        when(evolutions.findByEmployeIdOrderByDateEffetAscIdAsc(5L)).thenReturn(List.of(ev));
        RemunerationDto r = service.remuneration(5L);
        assertEquals(2, r.historique().size());
        assertEquals(new BigDecimal("1200.00"), r.salaireActuel());
        assertEquals(LocalDate.of(2024, 7, 1), r.dateEffetActuel());
    }

    @Test
    void evolutionEnregistreLeSalaireEnVigueurCommeAncienSalaire() {
        Contrat en = contrat(10L, ContratActe.INITIAL, ContratStatut.EN_COURS, "1500.00", LocalDate.of(2024, 1, 1), null);
        when(scope.peutVoirSalaire(5L)).thenReturn(true);
        when(contrats.findFirstByEmployeIdAndStatutContratInOrderByDateDebutDescIdDesc(eq(5L), any())).thenReturn(Optional.of(en));
        when(contrats.findByEmployeIdOrderByDateDebutDescIdDesc(5L)).thenReturn(List.of(en));
        when(evolutions.saveAndFlush(any(EvolutionSalaire.class))).thenAnswer(i -> i.getArgument(0));

        PointSalaire p = service.enregistrerEvolution(5L, new EvolutionSalaireRequest(LocalDate.of(2025, 6, 1),
                new BigDecimal("1800"), EvolutionSalaireType.AUGMENTATION, "Révision annuelle"));

        var capture = ArgumentCaptor.forClass(EvolutionSalaire.class);
        verify(evolutions).saveAndFlush(capture.capture());
        assertEquals(new BigDecimal("1500.00"), capture.getValue().getSalaireBrutAncien());
        assertEquals(new BigDecimal("1800.00"), capture.getValue().getSalaireBrutNouveau());
        assertEquals("EUR", capture.getValue().getDevise());
        assertEquals("EVOLUTION", p.origine());
    }

    @Test
    void evolutionSansChangementOuSansContratRefusee() {
        when(scope.peutVoirSalaire(5L)).thenReturn(true);
        var requete = new EvolutionSalaireRequest(LocalDate.of(2025, 6, 1), new BigDecimal("1500.00"), EvolutionSalaireType.CORRECTION, "x");
        var sansContrat = assertThrows(BusinessRuleException.class, () -> service.enregistrerEvolution(5L, requete));
        assertEquals("AUCUN_CONTRAT_EN_COURS", sansContrat.getCode());

        Contrat en = contrat(10L, ContratActe.INITIAL, ContratStatut.EN_COURS, "1500.00", LocalDate.of(2024, 1, 1), null);
        when(contrats.findFirstByEmployeIdAndStatutContratInOrderByDateDebutDescIdDesc(eq(5L), any())).thenReturn(Optional.of(en));
        when(contrats.findByEmployeIdOrderByDateDebutDescIdDesc(5L)).thenReturn(List.of(en));
        var inchange = assertThrows(BusinessRuleException.class, () -> service.enregistrerEvolution(5L, requete));
        assertEquals("SALAIRE_INCHANGE", inchange.getCode());
        verify(evolutions, never()).saveAndFlush(any());
    }

    private Contrat contrat(Long id, ContratActe acte, ContratStatut statut, String salaire, LocalDate debut, LocalDate fin) {
        Departement d = Departement.nouvelle();
        d.setLibelle("Informatique");
        Poste poste = Poste.nouvelle();
        poste.initialiser("DEV", "Développeur", null, d);
        ReflectionTestUtils.setField(poste, "id", 7L);
        Contrat c = Contrat.nouvelle();
        ReflectionTestUtils.setField(c, "id", id);
        c.setEmploye(employe);
        c.setPoste(poste);
        c.setTypeContrat(ContratType.CDI);
        c.setTypeActe(acte);
        c.setStatutContrat(statut);
        c.setSalaireBrut(new BigDecimal(salaire));
        c.setDevise("EUR");
        c.setDateDebut(debut);
        c.setDateFin(fin);
        return c;
    }
}
