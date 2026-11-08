Bloc 5-Dossier administratif du salarié
Centraliser identité, coordonnées, documents et situation professionnelle
Conserver contrats, affectations, salaire et historique de carrière.
Gérer les documents administratifs et leur validité.
Prévoir confidentialité, habilitations et Journal d'audit.
Le dossier salarié devient le référentiel central du SIRH.

Ce qui existe déjà au niveau de bloc 5 :

**Identité et situation professionnelle.** La table `EMPLOYE` est créée à l'embauche, avec matricule, civilité, nom, prénom, date de naissance, e-mail professionnel, téléphone, dates d'embauche et de sortie, motif de sortie et statut. La fiche salarié RH affiche le poste actuel, les contrats et l'onboarding.

**Contrats, affectations, carrière.**
- **Contrats :** chaque contrat reste en base (CDD, renouvellement, avenant, CDI), avec son salaire et son lien vers le contrat précédent.
- **Affectations :** un salarié change de poste via la page « Affectations ».
- **Pages de détail :** le contrat et l'affectation ont chacun une page de détail.
- **Fin de contrat :** elle est gérée.

**Documents (GED).**
- **Types :** CV, contrat signé, RIB, pièce d'identité, carte vitale, règlement…
- **Rattachement :** employé, contrat, poste ou tâche.
- **Versions :** les versions successives sont conservées.
- **Intégrité :** une empreinte SHA-256 est enregistrée à chaque dépôt.
- **Statuts :** actif, archivé, supprimé.
- **Antivirus :** un point d'extension est prévu.

**Confidentialité et journaux.**
- **Niveaux :** trois niveaux existent (interne, confidentiel, strict). Les documents d'identité, le RIB et le contrat sont confidentiels par défaut, et « strict » est réservé aux profils `DOCUMENT_CONSULTER_TOUS` et administrateur.
- **Journaux :** les connexions sont journalisées, ainsi que chaque consultation, téléchargement, dépôt et suppression de document.
- **Colonnes techniques :** chaque table porte « créé par / modifié par / version ».
- **Permissions :** `DOCUMENT_CONSULTER_PERSO`, `DOCUMENT_CONSULTER_TOUS` et `AUDIT_CONSULTER` existent. L'API d'administration des rôles et permissions aussi.

## Ce qu'il faut ajouter pour finir le Bloc 5

**1. Un vrai dossier central (le plus gros manque)**
- **Données manquantes :** `EMPLOYE` n'a ni adresse, ni e-mail et téléphone personnels, ni lieu de naissance, nationalité, sexe, situation familiale, n° de CIN, n° CNaPS, ni contact d'urgence ni coordonnées bancaires structurées.
- **Aucune modification possible :** il n'existe aucun endpoint pour modifier un salarié, la RH ne peut donc pas corriger une fiche et le salarié ne peut pas mettre à jour ses coordonnées.
- **Pas de page de dossier :** le salarié n'a pas de page « Mon dossier », et la RH n'a qu'une fiche partielle.
- **À créer :** une page de dossier à onglets : Identité, Coordonnées, Situation professionnelle, Contrats, Carrière, Rémunération, Documents, Intégration, Historique.

**2. Historique de carrière et salaire**
- **Carrière :** la carrière n'est pas reconstituée d'un seul tenant. Il faut une chronologie unique : embauche, avenants, mutations, changements de salaire, sortie.
- **Salaire :** il n'existe que dans les contrats. Soit on le déduit des avenants, soit on ajoute une table d'évolutions pour les augmentations sans avenant.
- **Sortie :** terminer un contrat passe le salarié à « sorti » mais ne renseigne ni la date ni le motif de sortie. À corriger.

**3. Documents administratifs et validité**
- **Données manquantes :** un document n'a ni date d'émission, ni date d'expiration, ni statut de validation (à valider, validé, refusé), ni validateur.
- **À ajouter :** de nouveaux types (diplôme, permis de travail, certificat médical, acte de naissance…) et une liste des pièces obligatoires avec ce qui manque.
- **Alertes d'expiration :** sur le modèle de l'alerte CDD.
- **Interface :** le composant `DocumentList` existe mais n'est utilisé dans aucune page. Il faut un onglet Documents avec dépôt, versions, téléchargement et validation.

**4. Confidentialité et habilitations**
- **Niveau de confidentialité :** personne ne peut le changer, et le niveau « strict » est inaccessible à la RH de base. Il faut décider qui voit quoi.
- **Champs sensibles :** salaire, coordonnées bancaires et n° CNaPS doivent être masqués selon les droits. Une permission dédiée (par exemple `SALAIRE_CONSULTER`) est à prévoir.
- **Administration des habilitations :** l'API existe, mais la page « Utilisateurs » est un simple message. À construire.

**5. Journal d'audit**
- **Ce qui existe :** seuls la connexion et l'accès aux documents sont tracés.
- **Ce qui manque :** le journal des modifications (qui a changé quoi, ancienne et nouvelle valeur) sur le dossier salarié, les contrats, les affectations, les documents et les rôles. La consultation d'un dossier sensible n'est pas tracée non plus.
- **Permission inutilisée :** `AUDIT_CONSULTER` n'est utilisée nulle part, et il n'y a ni API ni page pour consulter le journal.
- **À créer :** une table `JOURNAL_AUDIT` en ajout seul (comme les autres journaux), un service appelé par les actions sensibles, un endpoint et une page avec filtres.

## Ordre de travail proposé

1. Base de données : migration `015` (nouveaux champs du dossier, validité des documents, journal d'audit, nouvelles permissions).
2. Backend du dossier : modification des données, chronologie de carrière, masquage des champs sensibles.
3. Documents : validité, validation, pièces obligatoires, alertes. <<-- On est ici. 1,2 finie
4. Audit : écriture dans les actions sensibles, API et page de consultation.
5. Habilitations : page d'administration des rôles et permissions.
6. Frontend : dossier à onglets pour la RH, « Mon dossier » pour le salarié.
7. Tests et README.

