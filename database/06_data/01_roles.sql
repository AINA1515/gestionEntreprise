/* =============================================================================
   12. DONNEES DE REFERENCE
   ============================================================================= */
-- PROMPT === 12. Donnees de reference ===

INSERT INTO ROLE_RH (CODE, LIBELLE, DESCRIPTION) VALUES ('MANAGER', 'Manager / Chef de departement', 'Exprime les besoins, evalue les candidats, realise les taches d''accueil');
INSERT INTO ROLE_RH (CODE, LIBELLE, DESCRIPTION) VALUES ('CHARGE_RECRUTEMENT', 'Charge de recrutement RH', 'Qualifie les demandes, publie les offres, preselectionne, emet les propositions');
INSERT INTO ROLE_RH (CODE, LIBELLE, DESCRIPTION) VALUES ('DIRECTION', 'Direction / Validateur budgetaire', 'Arbitre les demandes, consulte le reporting');
INSERT INTO ROLE_RH (CODE, LIBELLE, DESCRIPTION) VALUES ('GESTIONNAIRE_RH', 'Gestionnaire RH / Administrateur du personnel', 'Convertit en employe, gere contrats et onboarding');
INSERT INTO ROLE_RH (CODE, LIBELLE, DESCRIPTION) VALUES ('CANDIDAT', 'Candidat (espace externe)', 'Postule, suit ses candidatures, repond aux propositions');
INSERT INTO ROLE_RH (CODE, LIBELLE, DESCRIPTION) VALUES ('SALARIE', 'Salarie / Employe', 'Portail interne : taches d''onboarding et documents personnels');
INSERT INTO ROLE_RH (CODE, LIBELLE, DESCRIPTION) VALUES ('IT', 'Service informatique', 'Taches d''onboarding IT');
INSERT INTO ROLE_RH (CODE, LIBELLE, DESCRIPTION) VALUES ('MOYENS_GENERAUX', 'Moyens generaux', 'Taches d''onboarding logistiques');
INSERT INTO ROLE_RH (CODE, LIBELLE, DESCRIPTION) VALUES ('ADMIN', 'Administrateur applicatif', 'Gestion des comptes, consultation de l''audit');

COMMIT;
