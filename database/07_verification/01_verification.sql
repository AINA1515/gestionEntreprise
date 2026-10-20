/* =============================================================================
   13. VERIFICATION
   ============================================================================= */
PROMPT === 13. Verification ===

SELECT OBJECT_TYPE, COUNT(*) AS NB
  FROM USER_OBJECTS
 WHERE OBJECT_TYPE IN ('TABLE','SEQUENCE','TRIGGER','FUNCTION','INDEX','PACKAGE','PACKAGE BODY','PROCEDURE')
 GROUP BY OBJECT_TYPE ORDER BY OBJECT_TYPE;

-- Objets invalides (doit retourner 0 ligne)
SELECT OBJECT_NAME, OBJECT_TYPE FROM USER_OBJECTS WHERE STATUS <> 'VALID';

-- Controle : aucun package ni procedure ne doit exister (0 ligne attendue)
SELECT OBJECT_NAME, OBJECT_TYPE FROM USER_OBJECTS WHERE OBJECT_TYPE IN ('PACKAGE','PACKAGE BODY','PROCEDURE');
