package com.itu.gestionRH.service.hiring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.itu.gestionRH.entity.enums.ContratStatut;
import com.itu.gestionRH.entity.enums.EmployeStatut;
import com.itu.gestionRH.entity.hiring.Contrat;
import com.itu.gestionRH.entity.organization.Employe;
import com.itu.gestionRH.entity.organization.Poste;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.repository.hiring.ContratRepository;
import com.itu.gestionRH.repository.organization.EmployeRepository;
import com.itu.gestionRH.repository.organization.PosteRepository;
import com.itu.gestionRH.workflow.PosteMachine;
import com.itu.gestionRH.workflow.WorkflowContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ContratSuiviServiceSortieTest {
    private Contrat contratEnCours(Employe e) {
        Contrat c = Contrat.nouvelle();
        ReflectionTestUtils.setField(c, "id", 1L);
        c.setEmploye(e);
        c.setPoste(Poste.nouvelle());
        c.setStatutContrat(ContratStatut.EN_COURS);
        c.setDateDebut(LocalDate.of(2024, 1, 1));
        return c;
    }

    private Employe salarie() {
        Employe e = Employe.nouvelle();
        ReflectionTestUtils.setField(e, "id", 5L);
        e.setStatutActivite(EmployeStatut.ACTIF);
        e.setDateEmbaucheInitiale(LocalDate.of(2024, 1, 1));
        return e;
    }

    @Test
    void laFinDuDernierContratRenseigneDateEtMotifDeSortie() {
        var contrats = mock(ContratRepository.class);
        var employes = mock(EmployeRepository.class);
        var e = salarie();
        var c = contratEnCours(e);
        when(contrats.findById(1L)).thenReturn(Optional.of(c));
        when(contrats.findByEmployeIdAndStatutContratIn(any(), any())).thenReturn(List.of(c));
        var service = new ContratSuiviService(contrats, mock(AffectationRepository.class), employes,
                mock(PosteRepository.class), mock(PosteMachine.class), mock(com.itu.gestionRH.service.audit.AuditService.class));

        service.terminer(1L, LocalDate.of(2025, 3, 31), "Démission",
                WorkflowContext.of("rh", Set.of(), Set.of(), "Fin de contrat"));

        assertEquals(EmployeStatut.SORTI, e.getStatutActivite());
        assertEquals(LocalDate.of(2025, 3, 31), e.getDateSortie());
        assertEquals("Démission", e.getMotifSortie());
    }

    @Test
    void sansMotifExplicitLeCommentaireDuContexteSertDeMotif() {
        var contrats = mock(ContratRepository.class);
        var e = salarie();
        var c = contratEnCours(e);
        when(contrats.findById(1L)).thenReturn(Optional.of(c));
        when(contrats.findByEmployeIdAndStatutContratIn(any(), any())).thenReturn(List.of(c));
        var service = new ContratSuiviService(contrats, mock(AffectationRepository.class), mock(EmployeRepository.class),
                mock(PosteRepository.class), mock(PosteMachine.class), mock(com.itu.gestionRH.service.audit.AuditService.class));

        service.terminer(1L, LocalDate.of(2025, 3, 31), WorkflowContext.of("rh", Set.of(), Set.of(), "Fin de contrat"));

        assertEquals("Fin de contrat", e.getMotifSortie());
        assertEquals(LocalDate.of(2025, 3, 31), e.getDateSortie());
    }
}
