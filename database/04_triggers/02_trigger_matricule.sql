/* =============================================================================
   10. REGLES METIER PORTEES PAR LA BASE
   ============================================================================= */
-- PROMPT === 10. Regles metier ===

-- Matricule : genere automatiquement s'il est vide, puis immuable.
CREATE OR REPLACE TRIGGER TBR_EMPLOYE_MATRICULE
BEFORE INSERT OR UPDATE OF MATRICULE ON EMPLOYE
FOR EACH ROW
BEGIN
  IF INSERTING THEN
    IF :NEW.MATRICULE IS NULL THEN
      :NEW.MATRICULE := FN_NEXT_MATRICULE(
                          EXTRACT(YEAR FROM NVL(:NEW.DATE_EMBAUCHE_INITIALE, SYSDATE)));
    END IF;
  ELSIF :OLD.MATRICULE <> :NEW.MATRICULE THEN
    RAISE_APPLICATION_ERROR(-20010, 'Le matricule d''un employe est immuable.');
  END IF;
END;
/
