// Libellés du dossier salarié (valeurs alignées sur les enums du backend).
export const TYPES_DOCUMENT = {
  PIECE_IDENTITE: 'Pièce d’identité (CIN)', RIB: 'RIB', ACTE_NAISSANCE: 'Acte de naissance', CV: 'CV', DIPLOME: 'Diplôme',
  CONTRAT_SIGNE: 'Contrat signé', AVENANT_SIGNE: 'Avenant signé', REGLEMENT_SIGNE: 'Règlement intérieur signé',
  CERTIFICAT_MEDICAL: 'Certificat médical', PERMIS_TRAVAIL: 'Permis de travail', JUSTIFICATIF_DOMICILE: 'Justificatif de domicile',
  CASIER_JUDICIAIRE: 'Casier judiciaire', ATTESTATION_CNAPS: 'Attestation CNaPS', CARTE_VITALE: 'Carte vitale', AUTRE: 'Autre',
}

export const SITUATIONS_FAMILIALES = {
  CELIBATAIRE: 'Célibataire', MARIE: 'Marié(e)', PACSE: 'Pacsé(e)', UNION_LIBRE: 'Union libre', SEPARE: 'Séparé(e)', DIVORCE: 'Divorcé(e)', VEUF: 'Veuf / veuve',
}

export const NIVEAUX_CONFIDENTIALITE = { INTERNE: 'Interne', CONFIDENTIEL: 'Confidentiel', STRICT: 'Strict' }

export const VALIDATIONS = {
  A_VALIDER: { label: 'À valider', variant: 'warning' }, VALIDE: { label: 'Validé', variant: 'success' }, REFUSE: { label: 'Refusé', variant: 'danger' },
}

export const STATUTS_PIECE = {
  MANQUANT: { label: 'Manquant', variant: 'danger' }, A_VALIDER: { label: 'À valider', variant: 'warning' }, VALIDE: { label: 'Validé', variant: 'success' },
  REFUSE: { label: 'Refusé', variant: 'danger' }, EXPIRE: { label: 'Expiré', variant: 'danger' }, EXPIRE_BIENTOT: { label: 'Expire bientôt', variant: 'warning' },
}

export const EVENEMENTS_CARRIERE = {
  EMBAUCHE: { label: 'Embauche', icon: 'person-plus', variant: 'success' }, CONTRAT: { label: 'Contrat', icon: 'file-earmark-text', variant: 'primary' },
  AVENANT: { label: 'Avenant', icon: 'file-earmark-diff', variant: 'primary' }, RENOUVELLEMENT: { label: 'Renouvellement', icon: 'arrow-repeat', variant: 'primary' },
  FIN_CONTRAT: { label: 'Fin de contrat', icon: 'file-earmark-x', variant: 'secondary' }, AFFECTATION: { label: 'Affectation', icon: 'briefcase', variant: 'info' },
  MUTATION: { label: 'Mutation', icon: 'arrow-left-right', variant: 'info' }, CHANGEMENT_SALAIRE: { label: 'Salaire', icon: 'cash-coin', variant: 'warning' },
  SORTIE: { label: 'Sortie', icon: 'box-arrow-right', variant: 'danger' },
}

export const EVOLUTIONS_SALAIRE = {
  AUGMENTATION: 'Augmentation', PROMOTION: 'Promotion', REVALORISATION: 'Revalorisation', CORRECTION: 'Correction', AUTRE: 'Autre',
}

export const ACTIONS_AUDIT = {
  CREATION: 'Création', MODIFICATION: 'Modification', SUPPRESSION: 'Suppression', CONSULTATION: 'Consultation', VALIDATION: 'Validation',
  REFUS: 'Refus', ATTRIBUTION: 'Attribution', RETRAIT: 'Retrait', EXPORT: 'Export',
}

export const CHAMPS = {
  nom: 'Nom', prenom: 'Prénom', civilite: 'Civilité', dateNaissance: 'Date de naissance', lieuNaissance: 'Lieu de naissance', nationalite: 'Nationalité',
  sexe: 'Sexe', situationFamiliale: 'Situation familiale', numCin: 'N° CIN', numCnaps: 'N° CNaPS', emailProfessionnel: 'E-mail professionnel',
  telephone: 'Téléphone', emailPersonnel: 'E-mail personnel', telephonePersonnel: 'Téléphone personnel', adresseLigne1: 'Adresse', adresseLigne2: 'Complément d’adresse',
  codePostal: 'Code postal', ville: 'Ville', pays: 'Pays', urgenceNom: 'Contact d’urgence', urgenceLien: 'Lien avec le contact', urgenceTelephone: 'Téléphone du contact',
  banqueNom: 'Banque', ribCodeBanque: 'Code banque', ribCodeGuichet: 'Code guichet', ribNumeroCompte: 'N° de compte', ribCle: 'Clé RIB', iban: 'IBAN',
  titulaireCompte: 'Titulaire du compte', salaireBrut: 'Salaire brut', statutContrat: 'Statut du contrat', statutActivite: 'Statut du salarié',
  statutValidation: 'Validation', confidentialite: 'Confidentialité', dateExpiration: 'Date d’expiration', dateEmission: 'Date d’émission',
  poste: 'Poste', dateEffet: 'Date d’effet', typeContrat: 'Type de contrat', typeActe: 'Acte', dateDebut: 'Début', dateFin: 'Fin',
  dateFinEffective: 'Fin effective', dateSortie: 'Date de sortie', motifSortie: 'Motif de sortie', typeEvolution: 'Type d’évolution', login: 'Login', roles: 'Rôles',
  employeId: 'Salarié lié', motDePasse: 'Mot de passe',
}
export const libelleChamp = (champ) => CHAMPS[champ] || champ || '—'
