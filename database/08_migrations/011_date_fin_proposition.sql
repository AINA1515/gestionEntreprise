-- Date de fin du contrat proposé, requise lors de la conversion pour les contrats à durée déterminée.
DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM USER_TAB_COLUMNS
   WHERE TABLE_NAME = 'PROPOSITION_EMBAUCHE' AND COLUMN_NAME = 'DATE_FIN_SOUHAITEE';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE PROPOSITION_EMBAUCHE ADD (DATE_FIN_SOUHAITEE DATE)';
  END IF;
END;
/
COMMIT;
