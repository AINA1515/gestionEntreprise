<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { cvService } from '@/services/cvService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import FormField from '@/components/ui/FormField.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const router = useRouter()
const { loading, error, run } = useAsyncAction()
const niveaux = ref([])
const typesEmploi = ref([])

const formationVide = () => ({ diplome: '', etablissement: '', annee: null })
const experienceVide = () => ({ poste: '', entreprise: '', periode: '', missions: '', niveau: '', typeEmploi: '' })

const cv = reactive({
  nom: '', prenom: '', email: '', telephone: '', adresse: '',
  profil: '',
  diplome: '', etablissement: '', annee: null,
  formations: [{ diplome: '', etablissement: '', annee: null }],
  experiences: [{ poste: '', entreprise: '', periode: '', missions: '', niveau: '', typeEmploi: '' }],
  poste: '', entreprise: '', periode: '', missions: '',
  competences: '', langues: '',
})

// Pré-remplit avec le CV existant s'il y en a un (l'erreur « non disponible » est ignorée).
onMounted(async () => {
  try { [niveaux.value, typesEmploi.value] = await Promise.all([cvService.niveauxFormation(), cvService.typesEmploi()]) }
  catch { /* les listes restent vides : le champ est facultatif */ }
  try {
    const existant = await cvService.consulter()
    Object.assign(cv, existant)
    // Les anciens CV n'avaient qu'une formation et une expérience à plat : on les reprend comme premier élément.
    if (existant.formations?.length) cv.formations = existant.formations.map(({ diplome, etablissement, annee }) => ({ diplome, etablissement, annee }))
    else if (existant.diplome || existant.etablissement || existant.annee) {
      cv.formations = [{ diplome: existant.diplome || '', etablissement: existant.etablissement || '', annee: existant.annee || null }]
    } else cv.formations = [formationVide()]
    if (existant.experiences?.length) cv.experiences = existant.experiences.map((e) => ({ ...experienceVide(), ...e }))
    else if (existant.poste || existant.entreprise || existant.periode || existant.missions) {
      cv.experiences = [{ ...experienceVide(), poste: existant.poste || '', entreprise: existant.entreprise || '', periode: existant.periode || '', missions: existant.missions || '' }]
    } else cv.experiences = [experienceVide()]
  } catch { /* pas de CV enregistré */ }
})

function ajouterFormation() { cv.formations.push(formationVide()) }
function supprimerFormation(index) {
  cv.formations.splice(index, 1)
  if (!cv.formations.length) ajouterFormation()
}
function ajouterExperience() { cv.experiences.push(experienceVide()) }
function supprimerExperience(index) {
  cv.experiences.splice(index, 1)
  if (!cv.experiences.length) ajouterExperience()
}

const rempli = (objet) => Object.values(objet).some((valeur) => valeur !== null && valeur !== '')

async function enregistrer() {
  // On n'enregistre pas les blocs laissés vides ; les champs « à plat » reprennent la première expérience (anciens lecteurs).
  const experiences = cv.experiences.filter(rempli)
  const premiere = experiences[0] ?? {}
  const payload = {
    ...cv,
    formations: cv.formations.filter(rempli),
    experiences,
    poste: premiere.poste || '', entreprise: premiere.entreprise || '', periode: premiere.periode || '', missions: premiere.missions || '',
  }
  const result = await run(() => cvService.enregistrer(payload))
  if (!error.value && result !== undefined) router.push({ name: 'candidat-cv' })
}
</script>

<template>
  <div class="col-lg-9 mx-auto">
    <RouterLink class="btn btn-link px-0 mb-3" :to="{ name: 'candidat-cv' }">
      <i class="bi bi-arrow-left me-1" aria-hidden="true"></i>Retour à mon espace candidat
    </RouterLink>

    <div class="mb-4">
      <h1 class="h2">Créer mon CV</h1>
      <p class="text-body-secondary mb-1">Complétez les rubriques ci-dessous pour préparer votre candidature.</p>
      <p class="form-text">Les champs marqués d'un * sont obligatoires.</p>
    </div>

    <AlertMessage :message="error" />

    <form @submit.prevent="enregistrer">
      <section class="card shadow-sm mb-4">
        <div class="card-header fw-semibold">1. Informations personnelles</div>
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-6"><FormField id="nom" label="Nom *"><input id="nom" v-model="cv.nom" class="form-control" required /></FormField></div>
            <div class="col-md-6"><FormField id="prenom" label="Prénom *"><input id="prenom" v-model="cv.prenom" class="form-control" required /></FormField></div>
            <div class="col-md-6"><FormField id="email" label="Adresse e-mail *"><input id="email" v-model="cv.email" type="email" class="form-control" required /></FormField></div>
            <div class="col-md-6"><FormField id="telephone" label="Téléphone *"><input id="telephone" v-model="cv.telephone" type="tel" class="form-control" required /></FormField></div>
            <div class="col-12"><FormField id="adresse" label="Adresse"><input id="adresse" v-model="cv.adresse" class="form-control" /></FormField></div>
          </div>
        </div>
      </section>

      <section class="card shadow-sm mb-4">
        <div class="card-header fw-semibold">2. Profil professionnel</div>
        <div class="card-body">
          <FormField id="profil" label="Présentez-vous en quelques lignes">
            <textarea id="profil" v-model="cv.profil" rows="4" class="form-control"
                      placeholder="Votre parcours, vos objectifs et vos points forts..."></textarea>
          </FormField>
        </div>
      </section>

      <section class="card shadow-sm mb-4">
        <div class="card-header fw-semibold">3. Formation et diplômes</div>
        <div class="card-body">
          <div v-for="(formation, index) in cv.formations" :key="index" class="border rounded p-3 mb-3">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h3 class="h6 mb-0">Formation {{ index + 1 }}</h3>
              <button class="btn btn-sm btn-outline-danger" type="button" :aria-label="`Supprimer la formation ${index + 1}`" @click="supprimerFormation(index)">
                <i class="bi bi-trash" aria-hidden="true"></i><span class="ms-1">Supprimer</span>
              </button>
            </div>
            <div class="row g-3">
              <div class="col-md-6"><FormField :id="`diplome-${index}`" label="Diplôme ou formation"><input :id="`diplome-${index}`" v-model="formation.diplome" class="form-control" placeholder="Ex. : Licence en informatique" /></FormField></div>
              <div class="col-md-6"><FormField :id="`etablissement-${index}`" label="Établissement"><input :id="`etablissement-${index}`" v-model="formation.etablissement" class="form-control" /></FormField></div>
              <div class="col-md-6"><FormField :id="`annee-${index}`" label="Année d'obtention"><input :id="`annee-${index}`" v-model.number="formation.annee" type="number" min="1950" max="2100" class="form-control" /></FormField></div>
            </div>
          </div>
          <button class="btn btn-outline-primary" type="button" @click="ajouterFormation"><i class="bi bi-plus-lg me-1" aria-hidden="true"></i>Ajouter une formation</button>
        </div>
      </section>

      <section class="card shadow-sm mb-4">
        <div class="card-header fw-semibold">4. Expériences professionnelles</div>
        <div class="card-body">
          <div v-for="(experience, index) in cv.experiences" :key="index" class="border rounded p-3 mb-3">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h3 class="h6 mb-0">Expérience {{ index + 1 }}</h3>
              <button class="btn btn-sm btn-outline-danger" type="button" :aria-label="`Supprimer l'expérience ${index + 1}`" @click="supprimerExperience(index)">
                <i class="bi bi-trash" aria-hidden="true"></i><span class="ms-1">Supprimer</span>
              </button>
            </div>
            <div class="row g-3">
              <div class="col-md-6"><FormField :id="`poste-${index}`" label="Poste occupé"><input :id="`poste-${index}`" v-model="experience.poste" class="form-control" /></FormField></div>
              <div class="col-md-6"><FormField :id="`entreprise-${index}`" label="Entreprise"><input :id="`entreprise-${index}`" v-model="experience.entreprise" class="form-control" /></FormField></div>
              <div class="col-md-4"><FormField :id="`periode-${index}`" label="Période"><input :id="`periode-${index}`" v-model="experience.periode" class="form-control" placeholder="Ex. : 2023 - 2025" /></FormField></div>
              <div class="col-md-4"><FormField :id="`type-emploi-${index}`" label="Type d'emploi"><select :id="`type-emploi-${index}`" v-model="experience.typeEmploi" class="form-select">
                <option value="">Sélectionner</option><option v-for="type in typesEmploi" :key="type.code" :value="type.code">{{ type.libelle }}</option>
              </select></FormField></div>
              <div class="col-md-4"><FormField :id="`niveau-${index}`" label="Niveau"><select :id="`niveau-${index}`" v-model="experience.niveau" class="form-select">
                <option value="">Sélectionner un niveau</option><option v-for="niveau in niveaux" :key="niveau.code" :value="niveau.code">{{ niveau.libelle }}</option>
              </select></FormField></div>
              <div class="col-12"><FormField :id="`missions-${index}`" label="Missions et réalisations"><textarea :id="`missions-${index}`" v-model="experience.missions" rows="3" class="form-control"></textarea></FormField></div>
            </div>
          </div>
          <button class="btn btn-outline-primary" type="button" @click="ajouterExperience"><i class="bi bi-plus-lg me-1" aria-hidden="true"></i>Ajouter une expérience</button>
        </div>
      </section>

      <section class="card shadow-sm mb-4">
        <div class="card-header fw-semibold">5. Compétences et langues</div>
        <div class="card-body">
          <FormField id="competences" label="Compétences">
            <textarea id="competences" v-model="cv.competences" rows="3" class="form-control"
                      placeholder="Ex. : Excel, gestion de projet, communication..."></textarea>
          </FormField>
          <FormField id="langues" label="Langues parlées">
            <textarea id="langues" v-model="cv.langues" rows="3" class="form-control"
                      placeholder="Ex. : Français, malgache, anglais..."></textarea>
          </FormField>
        </div>
      </section>

      <div class="d-flex flex-wrap gap-2 mb-3">
        <button class="btn btn-primary" type="submit" :disabled="loading">
          {{ loading ? 'Enregistrement…' : 'Enregistrer mon CV' }}
        </button>
        <RouterLink class="btn btn-outline-secondary" :to="{ name: 'candidat-cv' }">Annuler</RouterLink>
      </div>
    </form>
  </div>
</template>
