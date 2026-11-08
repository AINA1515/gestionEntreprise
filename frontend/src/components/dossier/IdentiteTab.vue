<script setup>
import { ref } from 'vue'
import { useFormatters } from '@/composables/useFormatters'
import { SITUATIONS_FAMILIALES } from '@/composables/useDossierLabels'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const props = defineProps({ dossier: { type: Object, required: true }, enregistrer: { type: Function, required: true }, saving: Boolean, erreur: { type: String, default: null } })
const { formatDate } = useFormatters()
const edition = ref(false)
const form = ref({})

function ouvrir() { form.value = { ...props.dossier.identite }; edition.value = true }
async function valider() { if (await props.enregistrer({ identite: form.value })) edition.value = false }
const sensible = (valeur) => valeur || 'Non renseigné'
</script>

<template>
  <section class="card"><div class="card-body">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2 class="h5 mb-0">Identité</h2>
      <button v-if="dossier.droits.modifiable && !edition" class="btn btn-sm btn-outline-primary" type="button" @click="ouvrir">Modifier</button>
    </div>

    <dl v-if="!edition" class="row mb-0">
      <dt class="col-sm-4">Civilité</dt><dd class="col-sm-8">{{ dossier.identite.civilite || '—' }}</dd>
      <dt class="col-sm-4">Nom et prénom</dt><dd class="col-sm-8">{{ dossier.identite.nom }} {{ dossier.identite.prenom }}</dd>
      <dt class="col-sm-4">Date de naissance</dt><dd class="col-sm-8">{{ formatDate(dossier.identite.dateNaissance) }}<span v-if="dossier.identite.lieuNaissance"> à {{ dossier.identite.lieuNaissance }}</span></dd>
      <dt class="col-sm-4">Nationalité</dt><dd class="col-sm-8">{{ dossier.identite.nationalite || '—' }}</dd>
      <dt class="col-sm-4">Sexe</dt><dd class="col-sm-8">{{ { M: 'Masculin', F: 'Féminin' }[dossier.identite.sexe] || '—' }}</dd>
      <dt class="col-sm-4">Situation familiale</dt><dd class="col-sm-8">{{ SITUATIONS_FAMILIALES[dossier.identite.situationFamiliale] || '—' }}</dd>
      <dt class="col-sm-4">N° CIN</dt><dd class="col-sm-8">{{ sensible(dossier.identite.numCin) }}</dd>
      <dt class="col-sm-4">N° CNaPS</dt><dd class="col-sm-8">{{ sensible(dossier.identite.numCnaps) }}</dd>
    </dl>
    <p v-if="!edition && !dossier.droits.donneesSensiblesVisibles" class="form-text mb-0 mt-2"><i class="bi bi-eye-slash me-1" aria-hidden="true"></i>Le CIN et le n° CNaPS sont masqués : votre profil n’est pas habilité à les voir en clair.</p>

    <form v-else-if="edition" @submit.prevent="valider">
      <AlertMessage :message="erreur" />
      <div class="row g-3">
        <div class="col-md-2"><label class="form-label" for="id-civ">Civilité</label><input id="id-civ" v-model="form.civilite" class="form-control" maxlength="10" /></div>
        <div class="col-md-5"><label class="form-label" for="id-nom">Nom</label><input id="id-nom" v-model="form.nom" class="form-control" maxlength="100" required /></div>
        <div class="col-md-5"><label class="form-label" for="id-prenom">Prénom</label><input id="id-prenom" v-model="form.prenom" class="form-control" maxlength="100" required /></div>
        <div class="col-md-4"><label class="form-label" for="id-dn">Date de naissance</label><input id="id-dn" v-model="form.dateNaissance" type="date" class="form-control" /></div>
        <div class="col-md-4"><label class="form-label" for="id-ln">Lieu de naissance</label><input id="id-ln" v-model="form.lieuNaissance" class="form-control" maxlength="100" /></div>
        <div class="col-md-4"><label class="form-label" for="id-nat">Nationalité</label><input id="id-nat" v-model="form.nationalite" class="form-control" maxlength="60" /></div>
        <div class="col-md-4"><label class="form-label" for="id-sexe">Sexe</label><select id="id-sexe" v-model="form.sexe" class="form-select"><option :value="null">—</option><option value="M">Masculin</option><option value="F">Féminin</option></select></div>
        <div class="col-md-4"><label class="form-label" for="id-sf">Situation familiale</label><select id="id-sf" v-model="form.situationFamiliale" class="form-select"><option :value="null">—</option><option v-for="(libelle, code) in SITUATIONS_FAMILIALES" :key="code" :value="code">{{ libelle }}</option></select></div>
        <div class="col-md-6"><label class="form-label" for="id-cin">N° CIN</label><input id="id-cin" v-model="form.numCin" class="form-control" maxlength="30" :disabled="!dossier.droits.donneesSensiblesVisibles" /></div>
        <div class="col-md-6"><label class="form-label" for="id-cnaps">N° CNaPS</label><input id="id-cnaps" v-model="form.numCnaps" class="form-control" maxlength="30" :disabled="!dossier.droits.donneesSensiblesVisibles" /></div>
      </div>
      <div class="mt-3 d-flex gap-2">
        <button class="btn btn-primary" type="submit" :disabled="saving">{{ saving ? 'Enregistrement…' : 'Enregistrer' }}</button>
        <button class="btn btn-outline-secondary" type="button" :disabled="saving" @click="edition = false">Annuler</button>
      </div>
    </form>
  </div></section>
</template>
