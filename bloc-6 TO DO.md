
Bloc 6-Temps de travail et pointage
Définir horaires, planning et régime de travail.
Enregistrer entrées, sorties, retards et heures supplémentaires.
Calculer le temps réellement travaillé.
Détecter les anomalies de pointage.
Les données de temps alimentent les absences et les variables de paie.

## Ce qui va et se réutilise tel quel
- **Qui pointe et qui valide :**
  - `EMPLOYE`, le lien compte ↔ salarié (`Utilisateur.employeId`) et le statut d'activité.
  - La hiérarchie : `AFFECTATION.superieur`, `DEPARTEMENT_RESPONSABLE`, et `AccessScope` pour limiter un manager à son équipe.
  - Le contrat en cours, qui dit si le salarié a le droit de pointer.
- **Droits :** rôles et permissions existants, et la page d'administration des habilitations du Bloc 5.
- **Traçabilité :** `AuditService` / `JOURNAL_AUDIT` pour toute correction de pointage, et le principe des journaux en ajout seul.
- **Alertes :** le planificateur quotidien et `NotificationService` (une notification par jour et par destinataire), déjà utilisés pour les fins de CDD et les pièces expirantes.
- **Justificatifs :** la GED (certificat médical, par exemple), avec rattachement à une anomalie.
- **Écrans :** « Mon espace » du salarié, les menus par rôle, le dossier à onglets (on y ajoutera un onglet « Temps de travail »).
- **Conventions :** migrations rejouables Oracle 11g, tables d'historique, tests Mockito.

## Ce qui manque (tout le Bloc 6)

**1. Base de données (migration 016)**
- **Régimes et horaires :**
  - `REGIME_TRAVAIL` : heures par semaine, par jour, jours travaillés, tolérance de retard, seuil d'heures supplémentaires.
  - Un modèle d'horaire par jour de la semaine (début, fin, pause).
  - Une affectation datée salarié ↔ régime ↔ horaire.
- **Planning :** `PLANNING` (jour par jour, par salarié : travail, repos, férié, exception) et `JOUR_FERIE`.
- **Pointage brut :** `POINTAGE` en ajout seul (salarié, horodatage, entrée ou sortie, source web / manuel / import, adresse IP). Un pointage ne se modifie jamais : une erreur se corrige par une écriture de correction avec motif.
- **Résultat calculé :** `JOURNEE_TRAVAIL` (minutes prévues, travaillées, retard, départ anticipé, heures supplémentaires par taux, nuit, dimanche, férié, statut de validation).
- **Contrôle :** `ANOMALIE_POINTAGE` (type, statut, justification, qui l'a traitée) et `DEMANDE_CORRECTION` (le salarié signale un oubli, le manager valide).
- **Paie et absences :** `PERIODE_TEMPS` (mois ouvert, validé, clôturé) et un export de variables de paie par salarié et par période.
- **Droits :** une dizaine de permissions (pointer, consulter soi / équipe / tous, corriger, gérer planning et régimes, valider les heures supplémentaires, traiter les anomalies, clôturer, exporter).

**2. Moteur de calcul**
Un service pur, facile à tester :
- apparier entrées et sorties ;
- déduire les pauses ;
- gérer les nuits à cheval sur minuit ;
- calculer retard, départ anticipé et temps réellement travaillé par rapport au planning ;
- calculer les heures supplémentaires (au jour et à la semaine) et leur ventilation nuit, dimanche et férié.

**3. API de pointage**
- **Pointer :** entrée et sortie du salarié, avec garde-fous (double entrée, sortie sans entrée, salarié non actif, contrat absent).
- **Corriger :** correction par le manager ou la RH, et demandes de correction.
- **Consulter :** feuille de temps du mois.
- **Valider et exporter :** validation des heures supplémentaires, clôture mensuelle, export.
- **Config RH :** gestion des régimes, horaires, planning et jours fériés.

**4. Détection des anomalies** (à chaque pointage et en tâche de nuit)
- oubli de sortie ;
- journée prévue sans aucun pointage (absence présumée) ;
- retards répétés ;
- durée excessive (par jour, par semaine) ;
- repos entre deux journées trop court ;
- pointage hors planning ou un jour férié ;
- doublons.

Les anomalies sont notifiées au manager et à la RH.

**5. Interface avec absences et paie**
Ces modules n'existent pas encore. Le Bloc 6 doit donc seulement produire des données propres et stables : la liste des absences constatées (jours prévus sans pointage, justifiables) et l'export des variables de paie par période clôturée. Je ne construirais ni la gestion des congés ni le bulletin.

**6. Frontend**
- **Salarié :** bouton « pointer » (entrée / sortie), feuille de temps du mois, demande de correction.
- **Manager :** présences du jour de son équipe, validation des corrections et des heures supplémentaires.
- **RH :** régimes, horaires, planning, anomalies, clôture et export, jours fériés.
- **Dossier :** onglet « Temps de travail ».

**7. Tests et README**

## Points à corriger dans l'existant
- **Devise :** `CONTRAT.DEVISE` vaut `EUR` par défaut en base, alors que le code écrit `MGA`. À aligner avant les variables de paie.
- **Contrat :** il n'a aucune notion d'heures hebdomadaires. Le régime sera donc une donnée séparée et datée, plutôt qu'une colonne ajoutée au contrat.

## Choix que je propose par défaut (tout est paramétrable)
- **Pointage :** par le salarié depuis l'application, avec saisie manuelle et import CSV prévus pour une badgeuse. Pas de géolocalisation.
- **Fuseau horaire :** Indian/Antananarivo (UTC+3, sans changement d'heure).
- **Régime standard :** 40 h par semaine, jours ouvrés lundi à vendredi.
- **Majorations d'heures supplémentaires :** taux configurables (je pars sur +30 % jusqu'à 8 h par semaine, +50 % au-delà, +30 % la nuit, +40 % dimanche et férié). Ces taux sont à confirmer avec votre RH ou le Code du travail malgache : je ne les garantis pas.
- **Validation :** les heures supplémentaires sont validées par le manager, puis la RH clôture le mois. Après clôture, plus de modification sans réouverture tracée.
- **Détails :** tolérance de retard de 10 minutes, et une journée sans pointage sur un jour prévu devient une anomalie « absence présumée ».
- **Ordre des travaux :** comme pour le Bloc 5.
  1. Migration 016.
  2. Moteur de calcul et pointage.
  3. Anomalies et notifications.
  4. Validation, clôture et export.
  5. Frontend.
  6. Tests et README.
