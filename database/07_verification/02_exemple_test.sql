/* =============================================================================
   EXEMPLE DE TEST (a executer a la main apres installation)
   Ce fichier n'est PAS appele par gestion_rh.sql.
   -----------------------------------------------------------------------------
   EXEC DBMS_SESSION.SET_IDENTIFIER('marie.rh');
   INSERT INTO CANDIDAT (NOM, PRENOM, EMAIL) VALUES ('Dupont','Jean','jean.dupont@mail.com');
   INSERT INTO CANDIDAT (NOM, PRENOM, EMAIL) VALUES ('Martin','Awa','awa.martin@mail.com');
   INSERT INTO EMPLOYE (NOM, PRENOM, CANDIDAT_ID, DATE_EMBAUCHE_INITIALE)
          SELECT NOM, PRENOM, ID, TRUNC(SYSDATE) FROM CANDIDAT;
   SELECT MATRICULE FROM EMPLOYE;                       -- EMP-2026-0001, EMP-2026-0002

   -- Historique du responsable d'un departement
   INSERT INTO DEPARTEMENT (CODE, LIBELLE, RESPONSABLE_ID)
          SELECT 'INFO', 'Informatique', MIN(ID) FROM EMPLOYE;
   UPDATE DEPARTEMENT SET RESPONSABLE_ID = (SELECT MAX(ID) FROM EMPLOYE) WHERE CODE = 'INFO';
   SELECT * FROM DEPARTEMENT_RESPONSABLE ORDER BY ID;   -- 2 lignes : la 1re cloturee, la 2e courante

   DELETE FROM EMPLOYE;                                 -- ORA-20002 : suppression interdite
   ROLLBACK;
   ============================================================================= */
