package com.itu.gestionRH.security;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.entity.recruitment.DemandeRecrutement;
import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.repository.hiring.AffectationRepository;
import com.itu.gestionRH.repository.recruitment.CandidatureRepository;
import com.itu.gestionRH.repository.recruitment.DemandeRecrutementRepository;
import com.itu.gestionRH.repository.security.UtilisateurRepository;
import com.itu.gestionRH.security.jwt.JwtAuthenticationFilter.JwtPrincipal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.mockito.Mockito.mock;

class AccessScopeTest {
    @AfterEach void clearContext(){SecurityContextHolder.clearContext();}
    @Test void managerNePeutPasLireLaDemandeDUnAutreManagerEtObtient404(){
        var demandes=mock(DemandeRecrutementRepository.class); var candidatures=mock(CandidatureRepository.class);
        var users=mock(UtilisateurRepository.class); var affectations=mock(AffectationRepository.class);
        var demande=DemandeRecrutement.nouvelle(); var proprietaire=mock(Utilisateur.class); when(proprietaire.getId()).thenReturn(42L); demande.setManager(proprietaire);
        when(demandes.findById(9L)).thenReturn(Optional.of(demande));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new JwtPrincipal(7L,"manager"),null,
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER"),new SimpleGrantedAuthority("PERM_DEMANDE_CONSULTER"))));
        var scope=new AccessScope(demandes,candidatures,users,affectations,mock(com.itu.gestionRH.repository.hiring.ContratRepository.class),
                mock(com.itu.gestionRH.repository.onboarding.OnboardingTacheRepository.class),mock(com.itu.gestionRH.repository.hiring.PropositionEmbaucheRepository.class),
                mock(com.itu.gestionRH.repository.documents.DocumentRepository.class));
        assertThrows(NotFoundException.class,()->scope.requireDemande(9L));
    }
}
