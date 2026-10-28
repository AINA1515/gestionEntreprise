/*
   Recale le compteur de matricules sur les matricules deja presents.
   Necessaire notamment apres l'insertion de salaries de demonstration avec des
   matricules fixes (EMP-AAAA-NNNN), qui ne passent pas par FN_NEXT_MATRICULE.
   GREATEST conserve un compteur deja superieur au maximum observe.
*/
MERGE INTO COMPTEUR_MATRICULE compteur
USING (
  SELECT TO_NUMBER(SUBSTR(MATRICULE, 5, 4)) AS ANNEE,
         MAX(TO_NUMBER(SUBSTR(MATRICULE, 10))) AS DERNIER_NUMERO
    FROM EMPLOYE
   WHERE REGEXP_LIKE(MATRICULE, '^EMP-[0-9]{4}-[0-9]{4,}$')
   GROUP BY SUBSTR(MATRICULE, 5, 4)
) existants
ON (compteur.ANNEE = existants.ANNEE)
WHEN MATCHED THEN UPDATE
  SET compteur.DERNIER_NUMERO = GREATEST(compteur.DERNIER_NUMERO, existants.DERNIER_NUMERO)
WHEN NOT MATCHED THEN INSERT (ANNEE, DERNIER_NUMERO)
  VALUES (existants.ANNEE, existants.DERNIER_NUMERO);

COMMIT;
