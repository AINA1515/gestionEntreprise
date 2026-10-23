// Libellés et couleurs des statuts : valeurs alignées sur les contraintes CHECK de la base.
const DEMANDE = {
  BROUILLON: { label: 'Brouillon', variant: 'secondary' },
  ATTENTE_RH: { label: 'En attente RH', variant: 'warning' },
  ATTENTE_DIRECTION: { label: 'En attente Direction', variant: 'warning' },
  VALIDEE: { label: 'Validée', variant: 'success' },
  REFUSEE: { label: 'Refusée', variant: 'danger' },
}

const CANDIDATURE = {
  RECUE: { label: 'Reçue', variant: 'secondary' },
  PRESELECTIONNEE: { label: 'Présélectionnée', variant: 'info' },
  EN_ENTRETIEN: { label: 'En entretien', variant: 'primary' },
  RETENUE: { label: 'Retenue', variant: 'success' },
  REJETEE: { label: 'Rejetée', variant: 'danger' },
  ANNULEE: { label: 'Annulée', variant: 'dark' },
}

const POSTE = {
  CREE: { label: 'Créé', variant: 'secondary' },
  VACANT: { label: 'Vacant', variant: 'warning' },
  POURVU: { label: 'Pourvu', variant: 'success' },
  FERME: { label: 'Fermé', variant: 'dark' },
}

const EMPLOYE = {
  EN_ONBOARDING: { label: 'En intégration', variant: 'info' },
  ACTIF: { label: 'Actif', variant: 'success' },
  SUSPENDU: { label: 'Suspendu', variant: 'warning' },
  SORTI: { label: 'Sorti', variant: 'secondary' },
}

const OFFRE = {
  BROUILLON: { label: 'Brouillon', variant: 'secondary' },
  PUBLIEE: { label: 'Publiée', variant: 'success' },
  CLOTUREE: { label: 'Clôturée', variant: 'dark' },
}

export const DECISIONS = [
  { value: 'FAVORABLE', label: 'Candidat retenu' },
  { value: 'DEFAVORABLE', label: 'Candidat non retenu' },
  { value: 'RESERVE', label: 'Décision en attente' },
]

export const TYPES_CONTRAT = [
  { value: 'CDI', label: 'CDI' },
  { value: 'CDD', label: 'CDD' },
  { value: 'STAGE', label: 'Stage' },
  { value: 'ALTERNANCE', label: 'Alternance' },
]

const TABLES = { demande: DEMANDE, candidature: CANDIDATURE, poste: POSTE, employe: EMPLOYE, offre: OFFRE }

export function useLabels() {
  function statut(type, value) {
    return TABLES[type]?.[value] ?? { label: value ?? '—', variant: 'secondary' }
  }
  return { statut }
}
