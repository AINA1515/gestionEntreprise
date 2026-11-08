package com.itu.gestionRH.service.recruitment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.controller.recruitment.dto.DemandeDtos.DemandeCreateRequest;
import com.itu.gestionRH.controller.recruitment.dto.DemandeDtos.ValidationRequest;
import com.itu.gestionRH.entity.enums.DemandeStatut;
import com.itu.gestionRH.entity.enums.TypeDemande;
import com.itu.gestionRH.entity.recruitment.DemandeRecrutement;
import com.itu.gestionRH.entity.recruitment.DemandeValidation;
import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.entity.organization.Departement;
import com.itu.gestionRH.entity.organization.Poste;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import com.itu.gestionRH.repository.organization.PosteRepository;
import com.itu.gestionRH.repository.recruitment.DemandeRecrutementRepository;
import com.itu.gestionRH.repository.recruitment.DemandeValidationRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.repository.security.NiveauFormationRepository;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.security.jwt.JwtAuthenticationFilter.JwtPrincipal;
import com.itu.gestionRH.workflow.DemandeRecrutementMachine;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class DemandeRecrutementServiceTest {
    private DemandeRecrutementRepository demandes;
    private DemandeValidationRepository validations;
    private PosteRepository postes;
    private EmployeRepository employes;
    private UtilisateurRepository utilisateurs;
    private AccessScope accessScope;
    private DemandeRecrutementService service;
    private DemandeRecrutement demande;
    private Poste poste;
    private Utilisateur manager;

    @BeforeEach
    void setUp() {
        demandes=mock(DemandeRecrutementRepository.class); validations=mock(DemandeValidationRepository.class);
        postes=mock(PosteRepository.class); employes=mock(EmployeRepository.class); utilisateurs=mock(UtilisateurRepository.class);
        accessScope=mock(AccessScope.class);
        var machine=new DemandeRecrutementMachine(mock(ApplicationEventPublisher.class));
        service=new DemandeRecrutementService(demandes, validations, postes, employes, utilisateurs, machine, accessScope, mock(NiveauFormationRepository.class), mock(com.itu.gestionRH.repository.security.TypeEmploiRepository.class));

        manager=utilisateur(10L, "Manager");
        Departement departement=Departement.nouvelle(); departement.setLibelle("Informatique"); departement.setEstActif(true);
        poste=Poste.nouvelle(); poste.initialiser("IT-DEV", "Développeur", null, departement);
        ReflectionTestUtils.setField(poste, "id", 7L);
        demande=DemandeRecrutement.nouvelle();
        demande.setPoste(poste); demande.setManager(manager); demande.setTypeDemande(TypeDemande.CREATION);
        demande.setEffectifDemande(2); demande.setMotif("Renfort équipe"); demande.setBudgetAlloue(BigDecimal.valueOf(50000));
        demande.setDateSouhaitee(LocalDate.now().plusMonths(2)); demande.initialiserEnBrouillon();
        ReflectionTestUtils.setField(demande, "id", 55L);
        when(postes.findById(7L)).thenReturn(Optional.of(poste));
        when(utilisateurs.findById(anyLong())).thenAnswer(inv -> Optional.of(inv.getArgument(0).equals(10L) ? manager : utilisateur((Long)inv.getArgument(0), "Validateur")));
        when(demandes.findById(55L)).thenReturn(Optional.of(demande));
        when(accessScope.requireDemande(55L)).thenReturn(demande);
        when(demandes.saveAndFlush(any())).thenAnswer(inv -> { DemandeRecrutement d=inv.getArgument(0); ReflectionTestUtils.setField(d, "id", 55L); return d; });
        when(demandes.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void clearSecurityContext() { SecurityContextHolder.clearContext(); }

    @Test
    void cycleCompletAjouteTroisLignesDHistorique() {
        authentifier(10L, "MANAGER", "DEMANDE_CREER", "DEMANDE_CONSULTER");
        var created=service.creer(new DemandeCreateRequest(7L, TypeDemande.CREATION, null, 2, "Renfort équipe",
                BigDecimal.valueOf(50000), LocalDate.now().plusMonths(2), null, null, false));
        assertEquals(DemandeStatut.BROUILLON, created.statut());
        service.soumettre(55L);
        assertEquals(DemandeStatut.ATTENTE_RH, demande.getStatutValidation());

        authentifier(20L, "GESTIONNAIRE_RH", "DEMANDE_VERIFIER_RH", "DEMANDE_CONSULTER");
        service.traiterValidation(55L, new ValidationRequest("TRANSMETTRE_DIRECTION", null));
        assertEquals(DemandeStatut.ATTENTE_DIRECTION, demande.getStatutValidation());

        authentifier(30L, "DIRECTION", "DEMANDE_APPROUVER_DIR", "DEMANDE_CONSULTER");
        service.traiterValidation(55L, new ValidationRequest("VALIDER", null));
        assertEquals(DemandeStatut.VALIDEE, demande.getStatutValidation());
        verify(validations, times(3)).save(any(DemandeValidation.class));
    }

    @Test
    void refusSansJustificationRetourneBadRequest() {
        demande.appliquerTransitionWorkflow(DemandeStatut.ATTENTE_DIRECTION);
        authentifier(30L, "DIRECTION", "DEMANDE_APPROUVER_DIR");
        BusinessRuleException exception=assertThrows(BusinessRuleException.class,
                () -> service.traiterValidation(55L, new ValidationRequest("REFUSER", "  ")));
        assertEquals("JUSTIFICATION_OBLIGATOIRE", exception.getCode());
        assertEquals(400, exception.getStatus().value());
        verify(validations, never()).save(any());
    }

    @Test
    void transitionInterditeDepuisBrouillonEstRefuseeParLeMoteur() {
        authentifier(30L, "DIRECTION", "DEMANDE_APPROUVER_DIR");
        BusinessRuleException exception=assertThrows(BusinessRuleException.class,
                () -> service.traiterValidation(55L, new ValidationRequest("VALIDER", null)));
        assertEquals("WORKFLOW_TRANSITION_INTERDITE", exception.getCode());
    }

    @Test
    void managerNePeutPasVoirLaDemandeDUnAutreManager() {
        authentifier(11L, "MANAGER", "DEMANDE_CREER", "DEMANDE_CONSULTER");
        assertThrows(NotFoundException.class, () -> service.obtenir(55L));
    }

    private void authentifier(Long userId, String role, String... permissions) {
        var authorities=new java.util.ArrayList<SimpleGrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        for (String permission : permissions) authorities.add(new SimpleGrantedAuthority("PERM_" + permission));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new JwtPrincipal(userId, "user"+userId), null, authorities));
    }
    private Utilisateur utilisateur(Long id, String nom) {
        Utilisateur utilisateur=Utilisateur.nouvelle(); ReflectionTestUtils.setField(utilisateur, "id", id); utilisateur.setNomAffichage(nom); return utilisateur;
    }
}
