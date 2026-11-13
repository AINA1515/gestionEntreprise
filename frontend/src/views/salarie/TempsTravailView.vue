<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { tempsTravailService } from '@/services/tempsTravailService'
import { employeService } from '@/services/employeService'

const employe = ref(null)
const creneaux = ref([])
const erreur = ref('')
const chargement = ref(false)
const declarationEnCours = ref(null)
const commentaires = ref({})
const heuresSup = ref({})

const aujourdHui = new Date()
const annee = ref(aujourdHui.getFullYear())
const mois = ref(aujourdHui.getMonth())
const jourSelectionne = ref(dateLocale(aujourdHui))
const nomsMois = Array.from({ length: 12 }, (_, index) => new Intl.DateTimeFormat('fr-FR', { month: 'long' }).format(new Date(2024, index, 1)))
const anneesDisponibles = Array.from({ length: 11 }, (_, index) => aujourdHui.getFullYear() - 5 + index)

function dateLocale(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
function decalageJour(date) { return (date.getDay() + 6) % 7 }
const joursCalendrier = computed(() => {
  const premier = new Date(annee.value, mois.value, 1)
  const nbJours = new Date(annee.value, mois.value + 1, 0).getDate()
  const total = Math.ceil((decalageJour(premier) + nbJours) / 7) * 7
  return Array.from({ length: total }, (_, index) => {
    const numero = index - decalageJour(premier) + 1
    const date = new Date(annee.value, mois.value, numero)
    return { date: dateLocale(date), numero: date.getDate(), dansMois: date.getMonth() === mois.value }
  })
})
const nombreParJour = computed(() => creneaux.value.reduce((compte, c) => {
  const date = c.dateDebut?.slice(0, 10)
  if (date) compte[date] = (compte[date] || 0) + 1
  return compte
}, {}))
const creneauxDuJour = computed(() => creneaux.value
  .filter((c) => c.dateDebut?.slice(0, 10) === jourSelectionne.value)
  .sort((a, b) => (a.dateDebut || '').localeCompare(b.dateDebut || '')))
const titreMois = computed(() => `${nomsMois[mois.value]} ${annee.value}`)
const titreJour = computed(() => new Intl.DateTimeFormat('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })
  .format(new Date(`${jourSelectionne.value}T12:00:00`)))

async function chargerMois() {
  erreur.value = ''
  if (!employe.value) return
  chargement.value = true
  const debut = `${annee.value}-${String(mois.value + 1).padStart(2, '0')}-01T00:00:00`
  const dernierJour = new Date(annee.value, mois.value + 1, 0).getDate()
  const fin = `${annee.value}-${String(mois.value + 1).padStart(2, '0')}-${String(dernierJour).padStart(2, '0')}T23:59:59`
  creneaux.value = []
  try { creneaux.value = await tempsTravailService.creneaux(employe.value.id, debut, fin) }
  catch (e) { erreur.value = e.message || 'Impossible de charger le calendrier de travail.' }
  finally { chargement.value = false }
}

watch([annee, mois], async () => {
  const numeroJour = Number(jourSelectionne.value.slice(8, 10))
  const dernierJour = new Date(annee.value, mois.value + 1, 0).getDate()
  const date = new Date(annee.value, mois.value, Math.min(numeroJour, dernierJour))
  jourSelectionne.value = dateLocale(date)
  await chargerMois()
})

function changerMois(delta) {
  const date = new Date(annee.value, mois.value + delta, 1)
  annee.value = date.getFullYear()
  mois.value = date.getMonth()
}
function libelleStatut(code) {
  return ({ NON_COMMENCE: 'À faire', TERMINE: 'Terminé', TERMINE_HEURES_SUP: 'Terminé avec heures supplémentaires', NON_TERMINE: 'Non terminé', A_REFAIRE: 'À refaire' })[code] || (code || 'À faire').replaceAll('_', ' ')
}
function classeStatut(c) {
  if (c.statutValidation === 'VALIDEE') return 'text-bg-success'
  if (c.statutValidation === 'REFUSEE' || c.statutEmploye === 'A_REFAIRE') return 'text-bg-danger'
  if (c.statutValidation === 'EN_ATTENTE') return 'text-bg-info'
  if (c.statutEmploye === 'TERMINE' || c.statutEmploye === 'TERMINE_HEURES_SUP') return 'text-bg-warning'
  return 'text-bg-secondary'
}
function statutVisible(c) {
  if (c.statutValidation === 'VALIDEE') return 'Travail déclaré et validé'
  if (c.statutValidation === 'REFUSEE' || c.statutEmploye === 'A_REFAIRE') return 'Déclaration refusée · à refaire'
  if (c.statutValidation === 'EN_ATTENTE' && c.statutEmploye !== 'NON_COMMENCE') return 'Déclaration en attente de validation'
  return libelleStatut(c.statutEmploye)
}
function heure(valeur) { return valeur?.slice(11, 16) || '—' }

async function declarer(c, statut) {
  const h = Number(heuresSup.value[c.id] || 0)
  erreur.value = ''
  if (statut === 'TERMINE_HEURES_SUP' && h <= 0) { erreur.value = 'Indiquez les heures supplémentaires avant de valider cette déclaration.'; return }
  declarationEnCours.value = c.id
  try {
    const maj = await tempsTravailService.declarerCreneau(c.id, statut, commentaires.value[c.id] || '')
    Object.assign(c, maj)
  } catch (e) { erreur.value = e.message || 'Votre déclaration n’a pas pu être envoyée. Réessayez.' }
  finally { declarationEnCours.value = null }
}

onMounted(async () => {
  try { employe.value = await employeService.moi(); await chargerMois() }
  catch (e) { erreur.value = e.message || 'Calendrier indisponible.' }
})
</script>

<template>
  <div class="container-fluid py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-2 mb-3">
      <div><h1 class="h3 mb-1">Mon temps de travail</h1><p class="text-body-secondary mb-0">Choisissez un jour pour consulter vos créneaux et déclarer votre travail.</p></div>
      <div class="d-flex gap-2 align-items-end">
        <div><label for="agenda-annee" class="form-label small mb-1">Année</label><select id="agenda-annee" v-model.number="annee" class="form-select"><option v-for="a in anneesDisponibles" :key="a" :value="a">{{ a }}</option></select></div>
        <div><label for="agenda-mois" class="form-label small mb-1">Mois</label><select id="agenda-mois" v-model.number="mois" class="form-select text-capitalize"><option v-for="(nom, index) in nomsMois" :key="nom" :value="index">{{ nom }}</option></select></div>
      </div>
    </div>

    <div v-if="erreur" class="alert alert-danger" role="alert"><strong>Action impossible.</strong> {{ erreur }}</div>

    <div class="row g-3 align-items-start">
      <section class="col-xl-7" aria-label="Calendrier mensuel">
        <div class="card shadow-sm">
          <div class="card-header bg-white d-flex justify-content-between align-items-center">
            <button class="btn btn-sm btn-outline-secondary" type="button" aria-label="Mois précédent" @click="changerMois(-1)"><i class="bi bi-chevron-left" aria-hidden="true"></i></button>
            <h2 class="h5 text-capitalize mb-0">{{ titreMois }}</h2>
            <button class="btn btn-sm btn-outline-secondary" type="button" aria-label="Mois suivant" @click="changerMois(1)"><i class="bi bi-chevron-right" aria-hidden="true"></i></button>
          </div>
          <div class="card-body p-2 p-sm-3">
            <div class="agenda-grid agenda-weekdays" aria-hidden="true"><span v-for="j in ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim']" :key="j">{{ j }}</span></div>
            <div class="agenda-grid" :aria-label="`Jours de ${titreMois}`">
              <button v-for="j in joursCalendrier" :key="j.date" type="button" class="agenda-day" :class="{ 'agenda-day-outside': !j.dansMois, 'agenda-day-selected': j.date === jourSelectionne, 'agenda-day-today': j.date === dateLocale(aujourdHui) }" :disabled="!j.dansMois || chargement" :aria-pressed="j.date === jourSelectionne" :aria-label="`${j.numero} ${nomsMois[Number(j.date.slice(5, 7)) - 1]}${nombreParJour[j.date] ? `, ${nombreParJour[j.date]} créneau(x)` : ''}`" @click="jourSelectionne = j.date">
                <span>{{ j.numero }}</span><span v-if="nombreParJour[j.date]" class="agenda-count">{{ nombreParJour[j.date] }}<span class="visually-hidden"> créneau(x)</span></span><span v-else class="agenda-dot-space"></span>
              </button>
            </div>
            <div v-if="chargement" class="small text-body-secondary text-center mt-2" role="status"><span class="spinner-border spinner-border-sm me-1" aria-hidden="true"></span>Chargement du mois…</div>
            <p class="small text-body-secondary mt-3 mb-0"><i class="bi bi-info-circle me-1" aria-hidden="true"></i>Le nombre dans une journée indique les créneaux prévus.</p>
          </div>
        </div>
      </section>

      <section class="col-xl-5" aria-live="polite">
        <div class="card shadow-sm">
          <div class="card-header bg-white"><h2 class="h5 text-capitalize mb-0">{{ titreJour }}</h2></div>
          <div class="card-body">
            <div v-if="!creneauxDuJour.length" class="text-center text-body-secondary py-4"><i class="bi bi-calendar2-check fs-3 d-block mb-2" aria-hidden="true"></i>Aucun créneau prévu pour cette journée.</div>
            <article v-for="c in creneauxDuJour" :key="c.id" class="border rounded p-3 mb-3 last-child-mb-0">
              <div class="d-flex justify-content-between align-items-start gap-2"><div><h3 class="h6 mb-1">{{ c.libelle || ({ TRAVAIL: 'Travail', FORMATION: 'Formation', PAUSE: 'Pause' }[c.typeCreneau] || 'Créneau de travail') }}</h3><p class="small text-body-secondary mb-2"><i class="bi bi-clock me-1" aria-hidden="true"></i>{{ heure(c.dateDebut) }} – {{ heure(c.dateFin) }}</p></div><span class="badge" :class="classeStatut(c)">{{ statutVisible(c) }}</span></div>
              <p v-if="c.commentaireEmploye" class="small bg-light rounded p-2 mb-2"><strong>Votre dernier commentaire :</strong> {{ c.commentaireEmploye }}</p>
              <div v-if="c.statutValidation !== 'VALIDEE'" class="border-top pt-2 mt-2">
                <label class="form-label small mb-1" :for="`commentaire-${c.id}`">Commentaire <span class="text-body-secondary">(facultatif)</span></label>
                <textarea :id="`commentaire-${c.id}`" v-model="commentaires[c.id]" class="form-control form-control-sm" rows="2" maxlength="500" placeholder="Précisez votre avancement si nécessaire"></textarea>
                <div v-if="c.statutEmploye === 'NON_TERMINE' || c.statutEmploye === 'A_REFAIRE'" class="mt-2"><label class="form-label small mb-1" :for="`heures-${c.id}`">Heures supplémentaires (heures)</label><input :id="`heures-${c.id}`" v-model.number="heuresSup[c.id]" type="number" min="0" step="0.5" class="form-control form-control-sm" placeholder="Ex. 1,5" /></div>
                <div class="d-flex flex-wrap gap-2 mt-2"><button class="btn btn-sm btn-success" type="button" :disabled="declarationEnCours === c.id" @click="declarer(c, 'TERMINE')"><span v-if="declarationEnCours === c.id" class="spinner-border spinner-border-sm me-1" aria-hidden="true"></span>Terminé</button><button v-if="c.statutEmploye === 'NON_TERMINE' || c.statutEmploye === 'A_REFAIRE'" class="btn btn-sm btn-outline-success" type="button" :disabled="declarationEnCours === c.id" @click="declarer(c, 'TERMINE_HEURES_SUP')">Terminé avec heures sup.</button><button class="btn btn-sm btn-outline-secondary" type="button" :disabled="declarationEnCours === c.id" @click="declarer(c, 'NON_TERMINE')">Pas terminé</button></div>
              </div>
            </article>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.agenda-grid { display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); gap: .35rem; }
.agenda-weekdays { margin-bottom: .35rem; text-align: center; color: var(--bs-secondary-color); font-size: .8rem; font-weight: 600; }
.agenda-day { min-height: 4.25rem; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: .2rem; border: 1px solid var(--bs-border-color); border-radius: .5rem; background: var(--bs-body-bg); color: var(--bs-body-color); }
.agenda-day:not(:disabled):hover { background: var(--bs-primary-bg-subtle); border-color: var(--bs-primary); }
.agenda-day-selected { color: var(--bs-primary-text-emphasis); background: var(--bs-primary-bg-subtle); border: 2px solid var(--bs-primary); font-weight: 700; }
.agenda-day-today:not(.agenda-day-selected) { border-color: var(--bs-primary); }
.agenda-day-outside { visibility: hidden; }
.agenda-count { min-width: 1.3rem; padding: .05rem .3rem; border-radius: 1rem; background: var(--bs-primary); color: white; font-size: .7rem; line-height: 1.2; }
.agenda-dot-space { height: 1rem; }
@media (max-width: 575.98px) { .agenda-grid { gap: .2rem; } .agenda-day { min-height: 3.4rem; border-radius: .35rem; } }
</style>
