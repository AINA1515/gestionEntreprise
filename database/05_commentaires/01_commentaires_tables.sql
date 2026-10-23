/* =============================================================================
   11. COMMENTAIRES (documentation du dictionnaire de donnees)
   ============================================================================= */
-- PROMPT === 11. Commentaires ===

COMMENT ON TABLE CANDIDAT IS 'Personne externe qui postule. Conserve son historique de candidatures meme apres embauche (CANDIDAT different de EMPLOYE). Contient le consentement RGPD.';
COMMENT ON TABLE EMPLOYE IS 'Dossier salarie cree a la conversion d''un candidat accepte. Porte le matricule unique et immuable (EMP-AAAA-NNNN).';
COMMENT ON TABLE UTILISATEUR IS 'Compte de connexion (interne ou candidat) lie a un employe ou a un candidat.';
COMMENT ON TABLE ROLE_RH IS 'Roles RBAC : Manager, Charge de recrutement, Direction, Gestionnaire RH, Candidat, Salarie, IT, Moyens generaux, Admin.';
COMMENT ON TABLE PERMISSION IS 'Droits unitaires (actions) du referentiel applicatif, groupes par module/bloc.';
COMMENT ON TABLE ROLE_PERMISSION IS 'Association role -> permissions (qui a le droit de faire quoi).';
COMMENT ON TABLE UTILISATEUR_ROLE IS 'Attribution datee des roles aux utilisateurs (debut/fin) : historique des habilitations.';
COMMENT ON TABLE DEPARTEMENT IS 'Departements / services, avec responsable et hierarchie optionnelle.';
COMMENT ON TABLE POSTE IS 'Poste de l''organigramme, independant de l''employe : peut etre VACANT sans titulaire.';
COMMENT ON TABLE DEMANDE_RECRUTEMENT IS 'Bloc 1 : demande de besoin en personnel (creation ou remplacement) emise par un manager.';
COMMENT ON TABLE DEMANDE_VALIDATION IS 'Bloc 1 : historique des decisions du circuit Manager -> RH -> Direction, avec justification obligatoire en cas de refus.';
COMMENT ON TABLE OFFRE_EMPLOI IS 'Bloc 2 : offre publiee a partir d''une demande validee.';
COMMENT ON TABLE CANDIDATURE IS 'Bloc 2 : lien offre/candidat avec son statut (recue, preselectionnee, en entretien, retenue, rejetee). Unique par couple offre+candidat.';
COMMENT ON TABLE ENTRETIEN IS 'Bloc 2 : planification des entretiens (RH, technique, final) et intervenant.';
COMMENT ON TABLE EVALUATION_CANDIDAT IS 'Bloc 2 : compte-rendu d''un entretien (note, avis, decision Favorable/Defavorable/Reserve).';
COMMENT ON TABLE EVALUATION_CRITERE IS 'Bloc 2 : detail de la grille d''evaluation (note ponderee par critere).';
COMMENT ON TABLE PROPOSITION_EMBAUCHE IS 'Bloc 3 : proposition d''embauche au finaliste, sa reponse et l''employe cree a l''acceptation.';
COMMENT ON TABLE CONTRAT IS 'Bloc 4 : un enregistrement par acte contractuel (initial, avenant, renouvellement). Historique jamais ecrase ; un seul contrat EN_COURS par employe.';
COMMENT ON TABLE AFFECTATION IS 'Historique des affectations employe/poste et lien hierarchique vers le superieur ; une seule affectation courante par employe.';
COMMENT ON TABLE MODELE_ONB_TACHE IS 'Catalogue de taches type (RH, IT, Moyens generaux, Manager, Salarie) servant a generer la checklist d''onboarding.';
COMMENT ON TABLE ONBOARDING IS 'Bloc 4 : parcours d''integration d''un employe (1 par employe) et son statut global.';
COMMENT ON TABLE ONBOARDING_TACHE IS 'Bloc 4 : taches de la checklist assignees aux acteurs, avec echeance et statut.';
COMMENT ON TABLE DOCUMENT IS 'Metadonnees GED : URL de stockage, empreinte SHA-256, versions, confidentialite. Rattachement polymorphe a CV, contrat, RIB, piece d''identite...';
COMMENT ON TABLE NOTIFICATION IS 'File d''attente des e-mails/notifications automatiques (etat, tentatives, erreurs).';
COMMENT ON TABLE DEPARTEMENT_RESPONSABLE IS 'Historique des responsables de departement (DATE_DEBUT / DATE_FIN). Alimente par le trigger TAH_DEPARTEMENT.';
COMMENT ON TABLE DEPARTEMENT_RATTACHEMENT IS 'Historique du rattachement hierarchique des departements (parent). Alimente par le trigger TAH_DEPARTEMENT.';
COMMENT ON TABLE POSTE_DEPARTEMENT IS 'Historique des departements de rattachement d''un poste. Alimente par le trigger TAH_POSTE.';
COMMENT ON TABLE CANDIDATURE_RECRUTEUR IS 'Historique des recruteurs en charge d''une candidature. Alimente par le trigger TAH_CANDIDATURE.';
COMMENT ON TABLE TACHE_ASSIGNATION IS 'Historique des assignations d''une tache d''onboarding. Alimente par le trigger TAH_ONBOARDING_TACHE.';
COMMENT ON TABLE COMPTEUR_MATRICULE IS 'Compteur annuel garantissant des matricules uniques et sans trou.';
COMMENT ON TABLE LOG_CONNEXION IS 'Journal des connexions, echecs, verrouillages et deconnexions. Ajout seul.';
COMMENT ON TABLE LOG_ACCES_DOCUMENT IS 'Journal des acces aux documents sensibles (consultation, telechargement, suppression). Ajout seul.';
