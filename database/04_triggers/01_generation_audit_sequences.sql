/* =============================================================================
   9. GENERATION AUTOMATIQUE : colonnes d'audit, sequences, triggers
   -----------------------------------------------------------------------------
   Pour chaque table metier :
     - ajout des 5 colonnes d'audit (N1)
     - sequence SEQ_<TABLE>
     - TBR_<TABLE> : BEFORE INSERT/UPDATE -> ID, dates, utilisateur, version
     - TBD_<TABLE> : interdit le DELETE physique (tables sensibles)
   Pour chaque table de journal : sequence, TBR_ (ID), TBX_ (UPDATE/DELETE interdits).
   ============================================================================= */
PROMPT === 9. Generation des triggers ===

DECLARE
  TYPE t_liste IS TABLE OF VARCHAR2(30);

  v_audites t_liste := t_liste(
    'ROLE_RH','PERMISSION','ROLE_PERMISSION','UTILISATEUR','UTILISATEUR_ROLE',
    'CANDIDAT','EMPLOYE','DEPARTEMENT','POSTE','DEMANDE_RECRUTEMENT','DEMANDE_VALIDATION',
    'OFFRE_EMPLOI','CANDIDATURE','ENTRETIEN','EVALUATION_CANDIDAT','EVALUATION_CRITERE',
    'PROPOSITION_EMBAUCHE','CONTRAT','AFFECTATION','MODELE_ONB_TACHE','ONBOARDING',
    'ONBOARDING_TACHE','DOCUMENT','NOTIFICATION',
    'DEPARTEMENT_RESPONSABLE','DEPARTEMENT_RATTACHEMENT','POSTE_DEPARTEMENT',
    'CANDIDATURE_RECRUTEUR','TACHE_ASSIGNATION');

  v_journaux t_liste := t_liste('LOG_CONNEXION','LOG_ACCES_DOCUMENT');

  v_sans_suppression t_liste := t_liste(
    'UTILISATEUR','CANDIDAT','EMPLOYE','POSTE','DEMANDE_RECRUTEMENT','DEMANDE_VALIDATION',
    'OFFRE_EMPLOI','CANDIDATURE','ENTRETIEN','EVALUATION_CANDIDAT','EVALUATION_CRITERE',
    'PROPOSITION_EMBAUCHE','CONTRAT','AFFECTATION','ONBOARDING','ONBOARDING_TACHE','DOCUMENT',
    'DEPARTEMENT_RESPONSABLE','DEPARTEMENT_RATTACHEMENT','POSTE_DEPARTEMENT',
    'CANDIDATURE_RECRUTEUR','TACHE_ASSIGNATION');

  v_t     VARCHAR2(30);
  v_sql   VARCHAR2(32767);
BEGIN
  ---------------------------------------------------------------------------
  -- A. Tables metier
  ---------------------------------------------------------------------------
  FOR i IN 1 .. v_audites.COUNT LOOP
    v_t := v_audites(i);

    EXECUTE IMMEDIATE 'ALTER TABLE ' || v_t || ' ADD ('
      || 'DATE_CREATION     TIMESTAMP(6)  DEFAULT SYSTIMESTAMP NOT NULL, '
      || 'CREE_PAR          VARCHAR2(100) DEFAULT USER NOT NULL, '
      || 'DATE_MODIFICATION TIMESTAMP(6), '
      || 'MODIFIE_PAR       VARCHAR2(100), '
      || 'VERSION_LIGNE     NUMBER(10)    DEFAULT 1 NOT NULL)';

    EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_' || v_t || ' START WITH 1 INCREMENT BY 1 CACHE 20 NOORDER';

    -- Trigger N1 : cle primaire + colonnes d'audit
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
    EXECUTE IMMEDIATE REPLACE(v_sql, '#T#', v_t);

    -- Pas de suppression physique sur les tables sensibles
    IF v_t MEMBER OF v_sans_suppression THEN
      v_sql := q'[CREATE OR REPLACE TRIGGER TBD_#T# BEFORE DELETE ON #T#
BEGIN
  RAISE_APPLICATION_ERROR(-20002, 'Suppression physique interdite sur #T# : utiliser le statut ou la date de fin (historique conserve).');
END;]';
      EXECUTE IMMEDIATE REPLACE(v_sql, '#T#', v_t);
    END IF;

    DBMS_OUTPUT.PUT_LINE('OK  table metier : ' || v_t);
  END LOOP;

  ---------------------------------------------------------------------------
  -- B. Tables de journal (ajout seul)
  ---------------------------------------------------------------------------
  FOR i IN 1 .. v_journaux.COUNT LOOP
    v_t := v_journaux(i);

    EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_' || v_t || ' START WITH 1 INCREMENT BY 1 CACHE 100 NOORDER';

    v_sql := q'[CREATE OR REPLACE TRIGGER TBR_#T# BEFORE INSERT ON #T# FOR EACH ROW
BEGIN
  IF :NEW.ID IS NULL THEN
    SELECT SEQ_#T#.NEXTVAL INTO :NEW.ID FROM DUAL;
  END IF;
END;]';
    EXECUTE IMMEDIATE REPLACE(v_sql, '#T#', v_t);

    v_sql := q'[CREATE OR REPLACE TRIGGER TBX_#T# BEFORE UPDATE OR DELETE ON #T#
BEGIN
  RAISE_APPLICATION_ERROR(-20003, 'Table de journal #T# : modification et suppression interdites.');
END;]';
    EXECUTE IMMEDIATE REPLACE(v_sql, '#T#', v_t);

    DBMS_OUTPUT.PUT_LINE('OK  table journal : ' || v_t);
  END LOOP;
END;
/
