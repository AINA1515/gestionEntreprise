/* Tous les salariés peuvent modifier leurs coordonnées et leur contact d'urgence.
   Les champs sensibles et l'identité restent soumis aux droits RH. */
INSERT INTO ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT r.ID, p.ID
FROM ROLE_RH r CROSS JOIN PERMISSION p
WHERE r.CODE IN ('SALARIE','IT','MOYENS_GENERAUX')
  AND p.CODE = 'DOSSIER_MODIFIER_PERSO'
  AND NOT EXISTS (SELECT 1 FROM ROLE_PERMISSION rp WHERE rp.ROLE_ID = r.ID AND rp.PERMISSION_ID = p.ID);
COMMIT;
