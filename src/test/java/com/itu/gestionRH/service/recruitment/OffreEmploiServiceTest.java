package com.itu.gestionRH.service.recruitment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.itu.gestionRH.common.BusinessRuleException;
import com.itu.gestionRH.controller.recruitment.dto.OffreDtos.OffreCreateRequest;
import com.itu.gestionRH.entity.enums.DemandeStatut;
import com.itu.gestionRH.entity.enums.OffreDiffusion;
import com.itu.gestionRH.entity.enums.OffreStatut;
import com.itu.gestionRH.entity.organization.Departement;
import com.itu.gestionRH.entity.organization.Poste;
import com.itu.gestionRH.entity.recruitment.DemandeRecrutement;
import com.itu.gestionRH.entity.recruitment.OffreEmploi;
import com.itu.gestionRH.repository.recruitment.CandidatureRepository;
import com.itu.gestionRH.repository.recruitment.DemandeRecrutementRepository;
import com.itu.gestionRH.repository.recruitment.OffreEmploiRepository;
import com.itu.gestionRH.workflow.OffreEmploiMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

class OffreEmploiServiceTest {
    private OffreEmploiRepository offres;
    private DemandeRecrutementRepository demandes;
    private CandidatureRepository candidatures;
    private OffreEmploiService service;
    private DemandeRecrutement demande;

    @BeforeEach
    void setUp() {
        offres = mock(OffreEmploiRepository.class);
        demandes = mock(DemandeRecrutementRepository.class);
        candidatures = mock(CandidatureRepository.class);
        service = new OffreEmploiService(offres, demandes, candidatures,
                new OffreEmploiMachine(mock(ApplicationEventPublisher.class)));
        demande = DemandeRecrutement.nouvelle();
        demande.appliquerTransitionWorkflow(DemandeStatut.VALIDEE);
        Departement departement = Departement.nouvelle();
        departement.setLibelle("Informatique");
        Poste poste = Poste.nouvelle();
        poste.initialiser("IT-01", "Développeur", "Développement logiciel", departement);
        demande.setPoste(poste);
        demande.setEffectifDemande(1);
        ReflectionTestUtils.setField(demande, "id", 41L);
        when(demandes.findById(41L)).thenReturn(Optional.of(demande));
        when(offres.countByDemandeIdAndStatutOffreIn(eq(41L), anyCollection())).thenReturn(0L);
        when(offres.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void refuseLaCreationQuandLaDemandeNestPasValidee() {
        demande.appliquerTransitionWorkflow(DemandeStatut.REFUSEE);
        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> service.creer(new OffreCreateRequest(41L, null, null, null, null, null, null, null, null)));
        assertEquals("DEMANDE_NON_VALIDEE", exception.getCode());
        verify(offres, never()).saveAndFlush(any());
    }

    @Test
    void creeUneOffreEnBrouillonEtLaPublieParLeWorkflow() {
        OffreEmploi offre = service.creer(new OffreCreateRequest(41L, null, null, null,
                OffreDiffusion.EXTERNE, null, "Antananarivo", null, null));
        assertEquals(OffreStatut.BROUILLON, offre.getStatutOffre());
        assertEquals(OffreDiffusion.EXTERNE, offre.getDiffusion());
        ReflectionTestUtils.setField(offre, "id", 9L);
        when(offres.findById(9L)).thenReturn(Optional.of(offre));

        service.publier(9L, WorkflowContext.of("recruteur", java.util.Set.of(), java.util.Set.of("OFFRE_PUBLIER"), null));
        assertEquals(OffreStatut.PUBLIEE, offre.getStatutOffre());
        assertEquals(LocalDate.now(), offre.getDatePublication());
    }

    @Test
    void clotureAutomatiqueTransitionneLesOffresExpirees() {
        OffreEmploi offre = OffreEmploi.nouvelle();
        offre.appliquerTransitionWorkflow(OffreStatut.PUBLIEE);
        offre.setDateLimiteCandidature(LocalDate.now().minusDays(1));
        ReflectionTestUtils.setField(offre, "id", 11L);
        when(offres.findByStatutOffreAndDateLimiteCandidatureBefore(OffreStatut.PUBLIEE, LocalDate.now()))
                .thenReturn(List.of(offre));

        int total = service.cloturerOffresExpirees(LocalDate.now());
        assertEquals(1, total);
        assertEquals(OffreStatut.CLOTUREE, offre.getStatutOffre());
        assertEquals(LocalDate.now(), offre.getDateCloture());
        verify(offres).flush();
    }
}
