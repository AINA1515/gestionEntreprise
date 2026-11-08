/* =============================================================================
   015 - BLOC 5 : DOSSIER ADMINISTRATIF DU SALARIE
   -----------------------------------------------------------------------------
   Contenu
     A. EMPLOYE            : identite complementaire, coordonnees, contact d'urgence,
                             coordonnees bancaires structurees, n° CIN / CNaPS.
     B. DOCUMENT           : dates d'emission / expiration, statut de validation,
                             validateur ; nouveaux types de documents.
     C. Nouvelles tables   : EVOLUTION_SALAIRE   (augmentations hors avenant)
                             DOCUMENT_OBLIGATOIRE (pieces exigees + delai d'alerte)
                             JOURNAL_AUDIT        (qui a change quoi, ajout seul)
     D. Donnees            : regularisation des sorties, reprise des coordonnees du
                             candidat, pieces obligatoires, permissions Bloc 5.
     E. Commentaires       : dictionnaire de donnees.

   Compatibilite : Oracle 11g. Script REJOUABLE (chaque objet est teste avant creation).
   Retro-compatible : toutes les colonnes ajoutees sont facultatives ou ont une valeur
   par defaut, les entites JPA actuelles (ddl-auto=validate) continuent donc de fonctionner
   avant l'ajout des nouveaux champs cote Java.
   A executer dans le schema applicatif, apres 014.
   ============================================================================= */

DECLARE
  ---------------------------------------------------------------------------
  -- Utilitaires de rejouabilite
  ---------------------------------------------------------------------------
  FUNCTION existe(p_requete VARCHAR2, p_a VARCHAR2 DEFAULT NULL, p_b VARCHAR2 DEFAULT NULL) RETURN BOOLEAN IS
    v_n NUMBER;
  BEGIN
    IF p_b IS NOT NULL THEN
      EXECUTE IMMEDIATE p_requete INTO v_n USING p_a, p_b;
    ELSE
      EXECUTE IMMEDIATE p_requete INTO v_n USING p_a;
    END IF;
    RETURN v_n > 0;
  END;

  PROCEDURE ajouter_colonne(p_table VARCHAR2, p_colonne VARCHAR2, p_definition VARCHAR2) IS
  BEGIN
    IF NOT existe('SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME = :1 AND COLUMN_NAME = :2',
                  p_table, p_colonne) THEN
      EXECUTE IMMEDIATE 'ALTER TABLE ' || p_table || ' ADD (' || p_colonne || ' ' || p_definition || ')';
    END IF;
  END;

  PROCEDURE ajouter_contrainte(p_table VARCHAR2, p_nom VARCHAR2, p_definition VARCHAR2,
                               p_remplacer BOOLEAN DEFAULT FALSE) IS
    v_presente BOOLEAN := existe('SELECT COUNT(*) FROM USER_CONSTRAINTS WHERE CONSTRAINT_NAME = :1', p_nom);
  BEGIN
    IF v_presente AND p_remplacer THEN
      EXECUTE IMMEDIATE 'ALTER TABLE ' || p_table || ' DROP CONSTRAINT ' || p_nom;
      v_presente := FALSE;
    END IF;
    IF NOT v_presente THEN
      EXECUTE IMMEDIATE 'ALTER TABLE ' || p_table || ' ADD CONSTRAINT ' || p_nom || ' ' || p_definition;
    END IF;
  END;

  PROCEDURE creer_index(p_nom VARCHAR2, p_definition VARCHAR2) IS
  BEGIN
    IF NOT existe('SELECT COUNT(*) FROM USER_INDEXES WHERE INDEX_NAME = :1', p_nom) THEN
      EXECUTE IMMEDIATE p_definition;
    END IF;
  END;

  PROCEDURE creer_table(p_table VARCHAR2, p_definition VARCHAR2) IS
  BEGIN
    IF NOT existe('SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = :1', p_table) THEN
      EXECUTE IMMEDIATE p_definition;
    END IF;
  END;

  -- Meme outillage que 04_triggers/01_generation_audit_sequences.sql :
  -- colonnes d'audit, sequence SEQ_<TABLE>, trigger TBR_<TABLE>, (option) TBD_<TABLE>.
  PROCEDURE outiller_table_metier(p_table VARCHAR2, p_sans_suppression BOOLEAN) IS
    v_sql VARCHAR2(4000);
  BEGIN
    ajouter_colonne(p_table, 'DATE_CREATION',     'TIMESTAMP(6)  DEFAULT SYSTIMESTAMP NOT NULL');
    ajouter_colonne(p_table, 'CREE_PAR',          'VARCHAR2(100) DEFAULT USER NOT NULL');
    ajouter_colonne(p_table, 'DATE_MODIFICATION', 'TIMESTAMP(6)');
    ajouter_colonne(p_table, 'MODIFIE_PAR',       'VARCHAR2(100)');
    ajouter_colonne(p_table, 'VERSION_LIGNE',     'NUMBER(10)    DEFAULT 1 NOT NULL');

    IF NOT existe('SELECT COUNT(*) FROM USER_SEQUENCES WHERE SEQUENCE_NAME = :1', 'SEQ_' || p_table) THEN
      EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_' || p_table || ' START WITH 1 INCREMENT BY 1 CACHE 20 NOORDER';
    END IF;

    v_sql := q'[CREATE OR REPLACE TRIGGER TBR_#T# BEFORE INSERT OR UPDATE ON #T# FOR EACH ROW
BEGIN
  IF INSERTING THEN
    IF :NEW.ID IS NULL THEN
      SELECT SEQ_#T#.NEXTVAL INTO :NEW.ID FROM DUAL;
    END IF;
    :NEW.DATE_CREATION     := SYSTIMESTAMP;
    :NEW.CREE_PAR          := FN_USER;
    :NEW.DATE_MODIFICATION := NULL;
    :NEW.MODIFIE_PAR       := NULL;
    :NEW.VERSION_LIGNE     := 1;
  ELSE
    :NEW.ID                := :OLD.ID;
    :NEW.DATE_CREATION     := :OLD.DATE_CREATION;
    :NEW.CREE_PAR          := :OLD.CREE_PAR;
    :NEW.DATE_MODIFICATION := SYSTIMESTAMP;
    :NEW.MODIFIE_PAR       := FN_USER;
    :NEW.VERSION_LIGNE     := NVL(:OLD.VERSION_LIGNE, 0) + 1;
  END IF;
END;]';
    EXECUTE IMMEDIATE REPLACE(v_sql, '#T#', p_table);

    IF p_sans_suppression THEN
      v_sql := q'[CREATE OR REPLACE TRIGGER TBD_#T# BEFORE DELETE ON #T#
BEGIN
  RAISE_APPLICATION_ERROR(-20002, 'Suppression physique interdite sur #T# : utiliser le statut ou la date de fin (historique conserve).');
END;]';
      EXECUTE IMMEDIATE REPLACE(v_sql, '#T#', p_table);
    END IF;
  END;

BEGIN
  ---------------------------------------------------------------------------
  -- A. EMPLOYE : dossier administratif complet
  ---------------------------------------------------------------------------
  -- Etat civil
  ajouter_colonne('EMPLOYE', 'LIEU_NAISSANCE',      'VARCHAR2(100)');
  ajouter_colonne('EMPLOYE', 'NATIONALITE',         'VARCHAR2(60)');
  ajouter_colonne('EMPLOYE', 'SEXE',                'VARCHAR2(1)');
  ajouter_colonne('EMPLOYE', 'SITUATION_FAMILIALE', 'VARCHAR2(15)');
  -- Identifiants administratifs (donnees sensibles)
  ajouter_colonne('EMPLOYE', 'NUM_CIN',             'VARCHAR2(30)');
  ajouter_colonne('EMPLOYE', 'NUM_CNAPS',           'VARCHAR2(30)');
  -- Coordonnees personnelles
  ajouter_colonne('EMPLOYE', 'EMAIL_PERSONNEL',     'VARCHAR2(150)');
  ajouter_colonne('EMPLOYE', 'TELEPHONE_PERSONNEL', 'VARCHAR2(30)');
  ajouter_colonne('EMPLOYE', 'ADRESSE_LIGNE1',      'VARCHAR2(300)');
  ajouter_colonne('EMPLOYE', 'ADRESSE_LIGNE2',      'VARCHAR2(200)');
  ajouter_colonne('EMPLOYE', 'CODE_POSTAL',         'VARCHAR2(10)');
  ajouter_colonne('EMPLOYE', 'VILLE',               'VARCHAR2(100)');
  ajouter_colonne('EMPLOYE', 'PAYS',                'VARCHAR2(60)');
  -- Contact d'urgence
  ajouter_colonne('EMPLOYE', 'URGENCE_NOM',         'VARCHAR2(150)');
  ajouter_colonne('EMPLOYE', 'URGENCE_LIEN',        'VARCHAR2(50)');
  ajouter_colonne('EMPLOYE', 'URGENCE_TELEPHONE',   'VARCHAR2(30)');
  -- Coordonnees bancaires structurees (donnees sensibles)
  ajouter_colonne('EMPLOYE', 'BANQUE_NOM',          'VARCHAR2(100)');
  ajouter_colonne('EMPLOYE', 'RIB_CODE_BANQUE',     'VARCHAR2(5)');
  ajouter_colonne('EMPLOYE', 'RIB_CODE_GUICHET',    'VARCHAR2(5)');
  ajouter_colonne('EMPLOYE', 'RIB_NUMERO_COMPTE',   'VARCHAR2(11)');
  ajouter_colonne('EMPLOYE', 'RIB_CLE',             'VARCHAR2(2)');
  ajouter_colonne('EMPLOYE', 'IBAN',                'VARCHAR2(34)');
  ajouter_colonne('EMPLOYE', 'TITULAIRE_COMPTE',    'VARCHAR2(150)');

  ajouter_contrainte('EMPLOYE', 'CK_EMP_SEXE',   'CHECK (SEXE IS NULL OR SEXE IN (''M'',''F''))');
  ajouter_contrainte('EMPLOYE', 'CK_EMP_SITFAM', 'CHECK (SITUATION_FAMILIALE IS NULL OR SITUATION_FAMILIALE IN '
    || '(''CELIBATAIRE'',''MARIE'',''PACSE'',''UNION_LIBRE'',''SEPARE'',''DIVORCE'',''VEUF''))');

  -- Un meme CIN / CNaPS ne peut identifier qu'un seul salarie (espaces ignores, NULL autorises).
  creer_index('UX_EMP_CIN',   'CREATE UNIQUE INDEX UX_EMP_CIN ON EMPLOYE (UPPER(REPLACE(NUM_CIN, '' '', '''')))');
  creer_index('UX_EMP_CNAPS', 'CREATE UNIQUE INDEX UX_EMP_CNAPS ON EMPLOYE (UPPER(REPLACE(NUM_CNAPS, '' '', '''')))');
  creer_index('IX_EMP_STATUT', 'CREATE INDEX IX_EMP_STATUT ON EMPLOYE (STATUT_ACTIVITE)');

  ---------------------------------------------------------------------------
  -- B. DOCUMENT : validite, validation, nouveaux types
  ---------------------------------------------------------------------------
  ajouter_colonne('DOCUMENT', 'DATE_EMISSION',     'DATE');
  ajouter_colonne('DOCUMENT', 'DATE_EXPIRATION',   'DATE');
  ajouter_colonne('DOCUMENT', 'STATUT_VALIDATION', 'VARCHAR2(10) DEFAULT ''A_VALIDER'' NOT NULL');
  ajouter_colonne('DOCUMENT', 'VALIDE_PAR_ID',     'NUMBER(10)');
  ajouter_colonne('DOCUMENT', 'DATE_VALIDATION',   'TIMESTAMP(6)');
  ajouter_colonne('DOCUMENT', 'MOTIF_REFUS',       'VARCHAR2(500)');

  -- Liste de types etendue (a garder alignee sur l'enum Java TypeDocument).
  ajouter_contrainte('DOCUMENT', 'CK_DOC_TYPE',
    'CHECK (TYPE_DOCUMENT IN (''CV'',''LETTRE_MOTIVATION'',''CONTRAT_SIGNE'',''RIB'','
    || '''PIECE_IDENTITE'',''CARTE_VITALE'',''REGLEMENT_SIGNE'',''FICHE_POSTE'',''PROPOSITION'','
    || '''DIPLOME'',''PERMIS_TRAVAIL'',''CERTIFICAT_MEDICAL'',''ACTE_NAISSANCE'','
    || '''JUSTIFICATIF_DOMICILE'',''CASIER_JUDICIAIRE'',''ATTESTATION_CNAPS'',''AVENANT_SIGNE'',''AUTRE''))',
    TRUE);

  ajouter_contrainte('DOCUMENT', 'CK_DOC_VALIDATION',
    'CHECK (STATUT_VALIDATION IN (''A_VALIDER'',''VALIDE'',''REFUSE''))');
  ajouter_contrainte('DOCUMENT', 'CK_DOC_VALIDITE',
    'CHECK (DATE_EXPIRATION IS NULL OR DATE_EMISSION IS NULL OR DATE_EXPIRATION >= DATE_EMISSION)');
  -- Une decision (validee ou refusee) est toujours signee et datee ; un refus est toujours motive.
  ajouter_contrainte('DOCUMENT', 'CK_DOC_DECISION',
    'CHECK (STATUT_VALIDATION = ''A_VALIDER'' OR (VALIDE_PAR_ID IS NOT NULL AND DATE_VALIDATION IS NOT NULL))');
  ajouter_contrainte('DOCUMENT', 'CK_DOC_REFUS',
    'CHECK (STATUT_VALIDATION <> ''REFUSE'' OR MOTIF_REFUS IS NOT NULL)');
  ajouter_contrainte('DOCUMENT', 'FK_DOC_VALIDEUR',
    'FOREIGN KEY (VALIDE_PAR_ID) REFERENCES UTILISATEUR (ID)');

  creer_index('IX_DOC_VALIDEUR',    'CREATE INDEX IX_DOC_VALIDEUR ON DOCUMENT (VALIDE_PAR_ID)');
  creer_index('IX_DOC_EXPIRATION',  'CREATE INDEX IX_DOC_EXPIRATION ON DOCUMENT (DATE_EXPIRATION, STATUT_DOCUMENT)');
  creer_index('IX_DOC_VALIDATION',  'CREATE INDEX IX_DOC_VALIDATION ON DOCUMENT (STATUT_VALIDATION, ENTITE_RATTACHEMENT)');

  ---------------------------------------------------------------------------
  -- C1. EVOLUTION_SALAIRE : changements de salaire sans avenant
  ---------------------------------------------------------------------------
  creer_table('EVOLUTION_SALAIRE', q'[CREATE TABLE EVOLUTION_SALAIRE (
  ID                    NUMBER(10)    NOT NULL,
  EMPLOYE_ID            NUMBER(10)    NOT NULL,
  CONTRAT_ID            NUMBER(10),
  DATE_EFFET            DATE          NOT NULL,
  SALAIRE_BRUT_ANCIEN   NUMBER(15,2),
  SALAIRE_BRUT_NOUVEAU  NUMBER(15,2)  NOT NULL,
  DEVISE                VARCHAR2(3)   DEFAULT 'EUR' NOT NULL,
  TYPE_EVOLUTION        VARCHAR2(15)  DEFAULT 'AUGMENTATION' NOT NULL,
  MOTIF                 VARCHAR2(500),
  CONSTRAINT PK_EVOLUTION_SALAIRE PRIMARY KEY (ID),
  CONSTRAINT FK_EVS_EMP FOREIGN KEY (EMPLOYE_ID) REFERENCES EMPLOYE (ID),
  CONSTRAINT FK_EVS_CTR FOREIGN KEY (CONTRAT_ID) REFERENCES CONTRAT (ID),
  CONSTRAINT CK_EVS_TYPE CHECK (TYPE_EVOLUTION IN
      ('AUGMENTATION','PROMOTION','REVALORISATION','CORRECTION','AUTRE')),
  CONSTRAINT CK_EVS_SAL CHECK (SALAIRE_BRUT_NOUVEAU >= 0 AND (SALAIRE_BRUT_ANCIEN IS NULL OR SALAIRE_BRUT_ANCIEN >= 0))
)]');
  creer_index('IX_EVS_EMP', 'CREATE INDEX IX_EVS_EMP ON EVOLUTION_SALAIRE (EMPLOYE_ID, DATE_EFFET)');
  creer_index('IX_EVS_CTR', 'CREATE INDEX IX_EVS_CTR ON EVOLUTION_SALAIRE (CONTRAT_ID)');
  outiller_table_metier('EVOLUTION_SALAIRE', TRUE);

  ---------------------------------------------------------------------------
  -- C2. DOCUMENT_OBLIGATOIRE : pieces exigees par type de contrat
  --     TYPE_CONTRAT NULL = exigee pour tous les contrats.
  --     Le type de document est controle par l'enum Java TypeDocument.
  ---------------------------------------------------------------------------
  creer_table('DOCUMENT_OBLIGATOIRE', q'[CREATE TABLE DOCUMENT_OBLIGATOIRE (
  ID                  NUMBER(10)    NOT NULL,
  TYPE_DOCUMENT       VARCHAR2(25)  NOT NULL,
  TYPE_CONTRAT        VARCHAR2(12),
  LIBELLE             VARCHAR2(150) NOT NULL,
  DELAI_ALERTE_JOURS  NUMBER(3)     DEFAULT 30 NOT NULL,
  ORDRE               NUMBER(3)     DEFAULT 0 NOT NULL,
  EST_ACTIF           NUMBER(1)     DEFAULT 1 NOT NULL,
  CONSTRAINT PK_DOCUMENT_OBLIGATOIRE PRIMARY KEY (ID),
  CONSTRAINT CK_DOBL_CONTRAT CHECK (TYPE_CONTRAT IS NULL OR TYPE_CONTRAT IN ('CDD','CDI','STAGE','ALTERNANCE')),
  CONSTRAINT CK_DOBL_ACTIF   CHECK (EST_ACTIF IN (0,1)),
  CONSTRAINT CK_DOBL_DELAI   CHECK (DELAI_ALERTE_JOURS BETWEEN 0 AND 365)
)]');
  creer_index('UX_DOBL_TYPE', 'CREATE UNIQUE INDEX UX_DOBL_TYPE ON DOCUMENT_OBLIGATOIRE (TYPE_DOCUMENT, NVL(TYPE_CONTRAT, ''*''))');
  outiller_table_metier('DOCUMENT_OBLIGATOIRE', FALSE);

  ---------------------------------------------------------------------------
  -- C3. JOURNAL_AUDIT : une ligne par champ modifie ou par consultation (ajout seul)
  --     EMPLOYE_ID = dossier concerne (filtre « historique du dossier »).
  --     EST_SENSIBLE = 1 : la valeur doit etre masquee a l'affichage sans habilitation
  --     (l'application peut aussi n'y stocker qu'une valeur masquee).
  --     OPERATION_ID regroupe les lignes d'une meme action utilisateur.
  ---------------------------------------------------------------------------
  creer_table('JOURNAL_AUDIT', q'[CREATE TABLE JOURNAL_AUDIT (
  ID               NUMBER(15)      NOT NULL,
  DATE_EVENEMENT   TIMESTAMP(6)    DEFAULT SYSTIMESTAMP NOT NULL,
  UTILISATEUR_ID   NUMBER(10),
  LOGIN            VARCHAR2(100)   NOT NULL,
  ACTION           VARCHAR2(15)    NOT NULL,
  ENTITE           VARCHAR2(30)    NOT NULL,
  ENTITE_ID        NUMBER(10),
  EMPLOYE_ID       NUMBER(10),
  CHAMP            VARCHAR2(60),
  ANCIENNE_VALEUR  VARCHAR2(2000),
  NOUVELLE_VALEUR  VARCHAR2(2000),
  EST_SENSIBLE     NUMBER(1)       DEFAULT 0 NOT NULL,
  MOTIF            VARCHAR2(500),
  OPERATION_ID     VARCHAR2(36),
  ADRESSE_IP       VARCHAR2(45),
  CONSTRAINT PK_JOURNAL_AUDIT PRIMARY KEY (ID),
  CONSTRAINT FK_JAUD_UTIL FOREIGN KEY (UTILISATEUR_ID) REFERENCES UTILISATEUR (ID),
  CONSTRAINT FK_JAUD_EMP  FOREIGN KEY (EMPLOYE_ID)     REFERENCES EMPLOYE (ID),
  CONSTRAINT CK_JAUD_ACTION CHECK (ACTION IN ('CREATION','MODIFICATION','SUPPRESSION','CONSULTATION',
      'VALIDATION','REFUS','ATTRIBUTION','RETRAIT','EXPORT')),
  CONSTRAINT CK_JAUD_SENS CHECK (EST_SENSIBLE IN (0,1))
)]');
  creer_index('IX_JAUD_DATE',   'CREATE INDEX IX_JAUD_DATE ON JOURNAL_AUDIT (DATE_EVENEMENT)');
  creer_index('IX_JAUD_EMP',    'CREATE INDEX IX_JAUD_EMP ON JOURNAL_AUDIT (EMPLOYE_ID, DATE_EVENEMENT)');
  creer_index('IX_JAUD_ENTITE', 'CREATE INDEX IX_JAUD_ENTITE ON JOURNAL_AUDIT (ENTITE, ENTITE_ID, DATE_EVENEMENT)');
  creer_index('IX_JAUD_LOGIN',  'CREATE INDEX IX_JAUD_LOGIN ON JOURNAL_AUDIT (LOGIN, DATE_EVENEMENT)');
  creer_index('IX_JAUD_UTIL',   'CREATE INDEX IX_JAUD_UTIL ON JOURNAL_AUDIT (UTILISATEUR_ID)');
  creer_index('IX_JAUD_OPER',   'CREATE INDEX IX_JAUD_OPER ON JOURNAL_AUDIT (OPERATION_ID)');

  IF NOT existe('SELECT COUNT(*) FROM USER_SEQUENCES WHERE SEQUENCE_NAME = :1', 'SEQ_JOURNAL_AUDIT') THEN
    EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_JOURNAL_AUDIT START WITH 1 INCREMENT BY 1 CACHE 100 NOORDER';
  END IF;

  -- Identifiant et, a defaut, utilisateur applicatif (FN_USER) renseignes par la base.
  EXECUTE IMMEDIATE q'[CREATE OR REPLACE TRIGGER TBR_JOURNAL_AUDIT BEFORE INSERT ON JOURNAL_AUDIT FOR EACH ROW
BEGIN
  IF :NEW.ID IS NULL THEN
    SELECT SEQ_JOURNAL_AUDIT.NEXTVAL INTO :NEW.ID FROM DUAL;
  END IF;
  IF :NEW.LOGIN IS NULL THEN
    :NEW.LOGIN := FN_USER;
  END IF;
END;]';

  EXECUTE IMMEDIATE q'[CREATE OR REPLACE TRIGGER TBX_JOURNAL_AUDIT BEFORE UPDATE OR DELETE ON JOURNAL_AUDIT
BEGIN
  RAISE_APPLICATION_ERROR(-20003, 'Table de journal JOURNAL_AUDIT : modification et suppression interdites.');
END;]';

  DBMS_OUTPUT.PUT_LINE('015 : structures du dossier salarie creees / verifiees.');
END;
/

/* =============================================================================
   D. DONNEES
   ============================================================================= */

-- D1. Sorties deja enregistrees sans date ni motif (la fin de contrat ne les renseignait pas).
--     Date retenue : fin effective du dernier contrat, sinon aujourd'hui.
UPDATE EMPLOYE e
   SET e.DATE_SORTIE = GREATEST(e.DATE_EMBAUCHE_INITIALE,
         NVL((SELECT MAX(NVL(c.DATE_FIN_EFFECTIVE, c.DATE_FIN)) FROM CONTRAT c WHERE c.EMPLOYE_ID = e.ID),
             TRUNC(SYSDATE))),
       e.MOTIF_SORTIE = NVL(e.MOTIF_SORTIE, 'Fin de contrat (regularise par la migration 015)')
 WHERE e.STATUT_ACTIVITE = 'SORTI'
   AND e.DATE_SORTIE IS NULL;

-- D2. Reprise des coordonnees saisies par le candidat avant son embauche (non anonymise).
UPDATE EMPLOYE e
   SET e.EMAIL_PERSONNEL     = (SELECT c.EMAIL     FROM CANDIDAT c WHERE c.ID = e.CANDIDAT_ID),
       e.TELEPHONE_PERSONNEL = (SELECT c.TELEPHONE FROM CANDIDAT c WHERE c.ID = e.CANDIDAT_ID),
       e.ADRESSE_LIGNE1      = (SELECT c.ADRESSE   FROM CANDIDAT c WHERE c.ID = e.CANDIDAT_ID)
 WHERE e.CANDIDAT_ID IS NOT NULL
   AND e.EMAIL_PERSONNEL IS NULL AND e.TELEPHONE_PERSONNEL IS NULL AND e.ADRESSE_LIGNE1 IS NULL
   AND EXISTS (SELECT 1 FROM CANDIDAT c WHERE c.ID = e.CANDIDAT_ID AND c.DATE_ANONYMISATION IS NULL);

-- D3. Pieces obligatoires de depart (inserees une seule fois : les reglages RH ulterieurs sont conserves).
MERGE INTO DOCUMENT_OBLIGATOIRE cible
USING (
  SELECT 'PIECE_IDENTITE' TYPE_DOCUMENT, 'Pièce d''identité (CIN)' LIBELLE, 30 DELAI_ALERTE_JOURS, 10 ORDRE FROM DUAL UNION ALL
  SELECT 'RIB',                'Relevé d''identité bancaire',        30, 20 FROM DUAL UNION ALL
  SELECT 'ACTE_NAISSANCE',     'Acte de naissance',                  30, 30 FROM DUAL UNION ALL
  SELECT 'CV',                 'Curriculum vitae',                   30, 40 FROM DUAL UNION ALL
  SELECT 'DIPLOME',            'Diplôme le plus élevé',              30, 50 FROM DUAL UNION ALL
  SELECT 'CONTRAT_SIGNE',      'Contrat de travail signé',           30, 60 FROM DUAL UNION ALL
  SELECT 'REGLEMENT_SIGNE',    'Règlement intérieur signé',          30, 70 FROM DUAL UNION ALL
  SELECT 'CERTIFICAT_MEDICAL', 'Certificat médical d''aptitude',     30, 80 FROM DUAL
) source
ON (cible.TYPE_DOCUMENT = source.TYPE_DOCUMENT AND cible.TYPE_CONTRAT IS NULL)
WHEN NOT MATCHED THEN INSERT (TYPE_DOCUMENT, TYPE_CONTRAT, LIBELLE, DELAI_ALERTE_JOURS, ORDRE, EST_ACTIF)
VALUES (source.TYPE_DOCUMENT, NULL, source.LIBELLE, source.DELAI_ALERTE_JOURS, source.ORDRE, 1);

-- D4. Nouvelles permissions du Bloc 5
MERGE INTO PERMISSION cible
USING (
  SELECT 'DOSSIER_CONSULTER_PERSO' CODE, 'Consulter son propre dossier salarié' LIBELLE, 'BLOC5' MODULE FROM DUAL UNION ALL
  SELECT 'DOSSIER_CONSULTER_TOUS',       'Consulter les dossiers de tous les salariés', 'BLOC5' FROM DUAL UNION ALL
  SELECT 'DOSSIER_MODIFIER_PERSO',       'Mettre à jour ses coordonnées et son contact d''urgence', 'BLOC5' FROM DUAL UNION ALL
  SELECT 'DOSSIER_MODIFIER',             'Modifier le dossier administratif d''un salarié (RH)', 'BLOC5' FROM DUAL UNION ALL
  SELECT 'SALAIRE_CONSULTER',            'Consulter la rémunération et l''historique salarial', 'BLOC5' FROM DUAL UNION ALL
  SELECT 'DONNEES_SENSIBLES_CONSULTER',  'Voir en clair CIN, n° CNaPS et coordonnées bancaires', 'BLOC5' FROM DUAL UNION ALL
  SELECT 'DOCUMENT_VALIDER',             'Valider ou refuser les pièces administratives', 'BLOC5' FROM DUAL UNION ALL
  SELECT 'DOCUMENT_CONFIDENTIALITE_GERER', 'Modifier le niveau de confidentialité d''un document', 'BLOC5' FROM DUAL UNION ALL
  SELECT 'DOCUMENT_STRICT_CONSULTER',    'Consulter les documents de niveau strict', 'BLOC5' FROM DUAL
) source
ON (cible.CODE = source.CODE)
WHEN MATCHED THEN UPDATE SET cible.LIBELLE = source.LIBELLE, cible.MODULE = source.MODULE
WHEN NOT MATCHED THEN INSERT (CODE, LIBELLE, MODULE)
VALUES (source.CODE, source.LIBELLE, source.MODULE);

-- D5. Attribution par defaut (ajustable depuis la future page d'administration des habilitations).
--     SALARIE : tout compte lie a un employe recoit ce role (migration 013).
--     DIRECTION n'obtient volontairement aucun acces nominatif aux dossiers ni aux salaires.
INSERT INTO ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT r.ID, p.ID
  FROM ROLE_RH r
  JOIN PERMISSION p ON (r.CODE, p.CODE) IN (
    ('SALARIE',         'DOSSIER_CONSULTER_PERSO'),
    ('SALARIE',         'DOSSIER_MODIFIER_PERSO'),
    ('GESTIONNAIRE_RH', 'DOSSIER_CONSULTER_TOUS'),
    ('GESTIONNAIRE_RH', 'DOSSIER_MODIFIER'),
    ('GESTIONNAIRE_RH', 'SALAIRE_CONSULTER'),
    ('GESTIONNAIRE_RH', 'DONNEES_SENSIBLES_CONSULTER'),
    ('GESTIONNAIRE_RH', 'DOCUMENT_VALIDER'),
    ('GESTIONNAIRE_RH', 'DOCUMENT_CONFIDENTIALITE_GERER'),
    ('ADMIN',           'DOCUMENT_STRICT_CONSULTER'))
 WHERE NOT EXISTS (SELECT 1 FROM ROLE_PERMISSION rp
                    WHERE rp.ROLE_ID = r.ID AND rp.PERMISSION_ID = p.ID);

COMMIT;

/* =============================================================================
   E. DICTIONNAIRE DE DONNEES
   ============================================================================= */
COMMENT ON TABLE EVOLUTION_SALAIRE IS 'Changements de salaire brut hors avenant (augmentation, promotion, correction). Complete l''historique porte par CONTRAT. Pas de suppression physique.';
COMMENT ON TABLE DOCUMENT_OBLIGATOIRE IS 'Pieces exigees dans un dossier salarie, par type de contrat (NULL = tous) ; sert a lister les pieces manquantes et a regler le delai d''alerte d''expiration.';
COMMENT ON TABLE JOURNAL_AUDIT IS 'Journal d''audit du dossier salarie : une ligne par champ modifie ou par consultation sensible (qui, quand, ancienne et nouvelle valeur). Ajout seul.';

COMMENT ON COLUMN EMPLOYE.NUM_CIN IS 'N° de carte d''identite nationale. Sensible : masque sans DONNEES_SENSIBLES_CONSULTER. Unique.';
COMMENT ON COLUMN EMPLOYE.NUM_CNAPS IS 'N° d''immatriculation CNaPS. Sensible : masque sans DONNEES_SENSIBLES_CONSULTER. Unique.';
COMMENT ON COLUMN EMPLOYE.EMAIL_PERSONNEL IS 'E-mail personnel (distinct de l''e-mail professionnel).';
COMMENT ON COLUMN EMPLOYE.SITUATION_FAMILIALE IS 'CELIBATAIRE, MARIE, PACSE, UNION_LIBRE, SEPARE, DIVORCE ou VEUF.';
COMMENT ON COLUMN EMPLOYE.RIB_CODE_BANQUE IS 'Coordonnees bancaires (RIB_*, IBAN, BANQUE_NOM, TITULAIRE_COMPTE) : sensibles, masquees sans DONNEES_SENSIBLES_CONSULTER.';

COMMENT ON COLUMN DOCUMENT.DATE_EMISSION IS 'Date d''emission du document par l''organisme emetteur.';
COMMENT ON COLUMN DOCUMENT.DATE_EXPIRATION IS 'Date de fin de validite ; declenche l''alerte d''expiration. NULL = sans echeance.';
COMMENT ON COLUMN DOCUMENT.STATUT_VALIDATION IS 'A_VALIDER (depot), VALIDE ou REFUSE (motif obligatoire). Concerne les pieces du dossier salarie.';
COMMENT ON COLUMN DOCUMENT.VALIDE_PAR_ID IS 'Utilisateur ayant valide ou refuse la piece.';
COMMENT ON COLUMN JOURNAL_AUDIT.EST_SENSIBLE IS '1 = valeur a masquer a l''affichage sans l''habilitation correspondante.';
COMMENT ON COLUMN JOURNAL_AUDIT.OPERATION_ID IS 'Identifiant commun aux lignes issues d''une meme action utilisateur.';
