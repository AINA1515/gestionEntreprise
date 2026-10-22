package com.itu.gestionRH.security;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import com.itu.gestionRH.config.SecurityConfig;
import com.itu.gestionRH.controller.hiring.ContratController;
import com.itu.gestionRH.controller.onboarding.OnboardingTacheController;
import com.itu.gestionRH.controller.recruitment.DemandeRecrutementController;
import com.itu.gestionRH.controller.recruitment.OffreEmploiController;
import com.itu.gestionRH.controller.security.AdminController;
import com.itu.gestionRH.controller.security.CandidatCandidatureController;
import com.itu.gestionRH.service.security.AdminService;
import com.itu.gestionRH.repository.recruitment.DemandeRecrutementRepository;
import com.itu.gestionRH.repository.recruitment.OffreEmploiRepository;
import com.itu.gestionRH.security.jwt.*;
import com.itu.gestionRH.workflow.DemandeRecrutementMachine;
import com.itu.gestionRH.workflow.OffreEmploiMachine;
import com.itu.gestionRH.workflow.OnboardingTacheMachine;
import com.itu.gestionRH.service.onboarding.OnboardingTacheService;
import com.itu.gestionRH.service.recruitment.DemandeRecrutementService;
import com.itu.gestionRH.repository.onboarding.OnboardingTacheRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest({DemandeRecrutementController.class, OffreEmploiController.class, ContratController.class,
        OnboardingTacheController.class, CandidatCandidatureController.class, AdminController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, RbacControllerTest.TxConfig.class})
class RbacControllerTest {
    @TestConfiguration static class TxConfig {
        @Bean org.springframework.transaction.PlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override protected Object doGetTransaction() { return new Object(); }
                @Override protected void doBegin(Object transaction, TransactionDefinition definition) {}
                @Override protected void doCommit(DefaultTransactionStatus status) {}
                @Override protected void doRollback(DefaultTransactionStatus status) {}
            };
        }
    }
    @Autowired MockMvc mvc;
    @MockitoBean JwtService jwtService;
    @MockitoBean DemandeRecrutementMachine demandeMachine;
    @MockitoBean DemandeRecrutementService demandeService;
    @MockitoBean DemandeRecrutementRepository demandeRepository;
    @MockitoBean OffreEmploiMachine offreMachine;
    @MockitoBean OffreEmploiRepository offreRepository;
    @MockitoBean AccessScope accessScope;
    @MockitoBean AdminService adminService;
    @MockitoBean OnboardingTacheRepository tacheRepository;
    @MockitoBean OnboardingTacheMachine tacheMachine;
    @MockitoBean OnboardingTacheService tacheService;

    @Test void chaqueRoleDisposeDUneRoutePermiseEtDUneRouteInterdite() throws Exception {
        when(adminService.utilisateurs()).thenReturn(List.of());
        when(accessScope.currentUserId()).thenReturn(1L);
        when(tacheRepository.findByAssigneeAId(eq(1L),any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());
        List<RoleCase> cases=List.of(
            new RoleCase("MANAGER","DEMANDE_CREER",post("/api/v1/demandes-recrutement","{\"posteId\":1,\"effectif\":1,\"motif\":\"Renfort\"}"),get("/api/v1/admin/utilisateurs")),
            new RoleCase("CHARGE_RECRUTEMENT","OFFRE_PUBLIER",post("/api/v1/recrutement/offres","{\"demandeId\":1,\"titre\":\"Poste\"}"),get("/api/v1/admin/utilisateurs")),
            new RoleCase("DIRECTION","DEMANDE_APPROUVER_DIR",patch("/api/v1/demandes-recrutement/1/validation","{\"statut\":\"VALIDEE\"}"),get("/api/v1/admin/utilisateurs")),
            new RoleCase("GESTIONNAIRE_RH","CONTRAT_GERER",post("/api/v1/onboarding/contrats","{}"),get("/api/v1/admin/utilisateurs")),
            new RoleCase("CANDIDAT","CANDIDATURE_POSTULER",get("/api/v1/candidat/candidatures"),get("/api/v1/admin/utilisateurs")),
            new RoleCase("SALARIE","TACHE_TRAITER",get("/api/v1/onboarding/taches/mes-taches"),get("/api/v1/admin/utilisateurs")),
            new RoleCase("IT","TACHE_TRAITER",get("/api/v1/onboarding/taches/mes-taches"),get("/api/v1/admin/utilisateurs")),
            new RoleCase("MOYENS_GENERAUX","TACHE_TRAITER",get("/api/v1/onboarding/taches/mes-taches"),get("/api/v1/admin/utilisateurs")),
            new RoleCase("ADMIN","UTILISATEUR_ADMIN",get("/api/v1/admin/utilisateurs"),get("/api/v1/candidat/candidatures")));
        for(RoleCase c:cases) {
            var auth=user("test").authorities(new SimpleGrantedAuthority("ROLE_"+c.role()),new SimpleGrantedAuthority("PERM_"+c.permission()));
            int allowedStatus=mvc.perform(c.allowed().with(auth)).andReturn().getResponse().getStatus();
            assertTrue(allowedStatus<500 || allowedStatus==501,c.role()+" autorisé, HTTP "+allowedStatus);
            assertEquals(403,mvc.perform(c.forbidden().with(auth)).andReturn().getResponse().getStatus(),c.role()+" interdit");
        }
    }
    private static MockHttpServletRequestBuilder post(String path,String body){return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(body);}
    private static MockHttpServletRequestBuilder patch(String path,String body){return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON).content(body);}
    private record RoleCase(String role,String permission,MockHttpServletRequestBuilder allowed,MockHttpServletRequestBuilder forbidden){}
}
