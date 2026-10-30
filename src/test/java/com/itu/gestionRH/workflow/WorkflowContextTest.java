package com.itu.gestionRH.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class WorkflowContextTest {
    @Test
    void avecPermissionConserveActeurEtDroitsExistants() {
        var manager = WorkflowContext.ofUtilisateur(7L, "manager", Set.of("MANAGER"), Set.of("EVALUATION_SAISIR"), "ok");
        assertFalse(manager.hasAccess(Set.of(), Set.of("PROPOSITION_EMETTRE")));

        var etendu = manager.avecPermission("PROPOSITION_EMETTRE");

        assertTrue(etendu.hasAccess(Set.of(), Set.of("PROPOSITION_EMETTRE")));
        assertTrue(etendu.hasAccess(Set.of(), Set.of("EVALUATION_SAISIR")));
        assertEquals(7L, etendu.utilisateurId());
        assertEquals("manager", etendu.acteur());
        // le contexte d'origine n'est pas modifié
        assertFalse(manager.permissions().contains("PROPOSITION_EMETTRE"));
    }
}
