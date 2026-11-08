<script setup>
import { computed, ref } from 'vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const props = defineProps({
  dossier: { type: Object, required: true }, mode: { type: String, default: 'moi' },
  enregistrer: { type: Function, required: true }, saving: Boolean, erreur: { type: String, default: null },
})
const edition = ref(false)
const form = ref({})
const d = computed(() => props.dossier)
const peutModifier = computed(() => d.value.droits.modifiable || d.value.droits.coordonneesModifiables)
const peutPro = computed(() => props.mode === 'rh' && d.value.droits.modifiable)
const peutBanque = computed(() => props.mode === 'rh' && d.value.droits.modifiable && d.value.droits.donneesSensiblesVisibles)

function ouvrir() {
  form.value = { coordonnees: { ...d.value.coordonnees }, contactUrgence: { ...d.value.contactUrgence }, coordonneesBancaires: { ...d.value.coordonneesBancaires } }
  edition.value = true
}
async function valider() {
  const partiel = { coordonnees: form.value.coordonnees, contactUrgence: form.value.contactUrgence }
  if (peutBanque.value) partiel.coordonneesBancaires = form.value.coordonneesBancaires
  if (await props.enregistrer(partiel)) edition.value = false
}
const v = (valeur) => valeur || 'Non renseigné'
</script>

<template>
  <section class="card"><div class="card-body">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2 class="h5 mb-0">Coordonnées</h2>
      <button v-if="peutModifier && !edition" class="btn btn-sm btn-outline-primary" type="button" @click="ouvrir">Modifier</button>
    </div>

    <template v-if="!edition">
      <h3 class="h6 text-body-secondary">Professionnelles</h3>
      <dl class="row"><dt class="col-sm-4">E-mail</dt><dd class="col-sm-8">{{ v(d.coordonnees.emailProfessionnel) }}</dd><dt class="col-sm-4">Téléphone</dt><dd class="col-sm-8">{{ v(d.coordonnees.telephone) }}</dd></dl>
      <h3 class="h6 text-body-secondary">Personnelles</h3>
      <dl class="row">
        <dt class="col-sm-4">E-mail</dt><dd class="col-sm-8">{{ v(d.coordonnees.emailPersonnel) }}</dd>
        <dt class="col-sm-4">Téléphone</dt><dd class="col-sm-8">{{ v(d.coordonnees.telephonePersonnel) }}</dd>
        <dt class="col-sm-4">Adresse</dt>
        <dd class="col-sm-8">
          <template v-if="d.coordonnees.adresseLigne1 || d.coordonnees.ville">
            {{ d.coordonnees.adresseLigne1 }}<br v-if="d.coordonnees.adresseLigne2" />{{ d.coordonnees.adresseLigne2 }}<br />{{ d.coordonnees.codePostal }} {{ d.coordonnees.ville }}<span v-if="d.coordonnees.pays"> · {{ d.coordonnees.pays }}</span>
          </template>
          <template v-else>Non renseignée</template>
        </dd>
      </dl>
      <h3 class="h6 text-body-secondary">Contact d’urgence</h3>
      <dl class="row"><dt class="col-sm-4">Nom</dt><dd class="col-sm-8">{{ v(d.contactUrgence.nom) }}<span v-if="d.contactUrgence.lien"> ({{ d.contactUrgence.lien }})</span></dd><dt class="col-sm-4">Téléphone</dt><dd class="col-sm-8">{{ v(d.contactUrgence.telephone) }}</dd></dl>
      <h3 class="h6 text-body-secondary">Coordonnées bancaires</h3>
      <dl class="row mb-0">
        <dt class="col-sm-4">Banque</dt><dd class="col-sm-8">{{ v(d.coordonneesBancaires.banqueNom) }}</dd>
        <dt class="col-sm-4">Titulaire</dt><dd class="col-sm-8">{{ v(d.coordonneesBancaires.titulaire) }}</dd>
        <dt class="col-sm-4">RIB</dt><dd class="col-sm-8">{{ [d.coordonneesBancaires.codeBanque, d.coordonneesBancaires.codeGuichet, d.coordonneesBancaires.numeroCompte, d.coordonneesBancaires.cle].filter(Boolean).join(' ') || 'Non renseigné' }}</dd>
        <dt class="col-sm-4">IBAN</dt><dd class="col-sm-8">{{ v(d.coordonneesBancaires.iban) }}</dd>
      </dl>
      <p v-if="!d.droits.donneesSensiblesVisibles" class="form-text mt-2 mb-0"><i class="bi bi-eye-slash me-1" aria-hidden="true"></i>Les coordonnées bancaires sont masquées pour votre profil.</p>
      <p v-else-if="mode === 'moi'" class="form-text mt-2 mb-0">Pour modifier vos coordonnées bancaires, contactez les Ressources humaines avec un justificatif.</p>
    </template>

    <form v-else @submit.prevent="valider">
      <AlertMessage :message="erreur" />
      <fieldset v-if="peutPro" class="mb-3"><legend class="h6">Professionnelles</legend>
        <div class="row g-3">
          <div class="col-md-6"><label class="form-label" for="co-emailpro">E-mail professionnel</label><input id="co-emailpro" v-model="form.coordonnees.emailProfessionnel" type="email" class="form-control" maxlength="150" /></div>
          <div class="col-md-6"><label class="form-label" for="co-tel">Téléphone</label><input id="co-tel" v-model="form.coordonnees.telephone" class="form-control" maxlength="30" /></div>
        </div>
      </fieldset>
      <fieldset class="mb-3"><legend class="h6">Personnelles</legend>
        <div class="row g-3">
          <div class="col-md-6"><label class="form-label" for="co-email">E-mail personnel</label><input id="co-email" v-model="form.coordonnees.emailPersonnel" type="email" class="form-control" maxlength="150" /></div>
          <div class="col-md-6"><label class="form-label" for="co-telp">Téléphone personnel</label><input id="co-telp" v-model="form.coordonnees.telephonePersonnel" class="form-control" maxlength="30" /></div>
          <div class="col-md-8"><label class="form-label" for="co-a1">Adresse</label><input id="co-a1" v-model="form.coordonnees.adresseLigne1" class="form-control" maxlength="300" /></div>
          <div class="col-md-4"><label class="form-label" for="co-a2">Complément</label><input id="co-a2" v-model="form.coordonnees.adresseLigne2" class="form-control" maxlength="200" /></div>
          <div class="col-md-3"><label class="form-label" for="co-cp">Code postal</label><input id="co-cp" v-model="form.coordonnees.codePostal" class="form-control" maxlength="10" /></div>
          <div class="col-md-5"><label class="form-label" for="co-ville">Ville</label><input id="co-ville" v-model="form.coordonnees.ville" class="form-control" maxlength="100" /></div>
          <div class="col-md-4"><label class="form-label" for="co-pays">Pays</label><input id="co-pays" v-model="form.coordonnees.pays" class="form-control" maxlength="60" /></div>
        </div>
      </fieldset>
      <fieldset class="mb-3"><legend class="h6">Contact d’urgence</legend>
        <div class="row g-3">
          <div class="col-md-5"><label class="form-label" for="co-un">Nom</label><input id="co-un" v-model="form.contactUrgence.nom" class="form-control" maxlength="150" /></div>
          <div class="col-md-3"><label class="form-label" for="co-ul">Lien</label><input id="co-ul" v-model="form.contactUrgence.lien" class="form-control" maxlength="50" /></div>
          <div class="col-md-4"><label class="form-label" for="co-ut">Téléphone</label><input id="co-ut" v-model="form.contactUrgence.telephone" class="form-control" maxlength="30" /></div>
        </div>
      </fieldset>
      <fieldset v-if="peutBanque" class="mb-3"><legend class="h6">Coordonnées bancaires</legend>
        <div class="row g-3">
          <div class="col-md-6"><label class="form-label" for="co-bn">Banque</label><input id="co-bn" v-model="form.coordonneesBancaires.banqueNom" class="form-control" maxlength="100" /></div>
          <div class="col-md-6"><label class="form-label" for="co-tit">Titulaire du compte</label><input id="co-tit" v-model="form.coordonneesBancaires.titulaire" class="form-control" maxlength="150" /></div>
          <div class="col-md-3"><label class="form-label" for="co-cb">Code banque</label><input id="co-cb" v-model="form.coordonneesBancaires.codeBanque" class="form-control" maxlength="5" inputmode="numeric" /></div>
          <div class="col-md-3"><label class="form-label" for="co-cg">Code guichet</label><input id="co-cg" v-model="form.coordonneesBancaires.codeGuichet" class="form-control" maxlength="5" inputmode="numeric" /></div>
          <div class="col-md-4"><label class="form-label" for="co-nc">N° de compte</label><input id="co-nc" v-model="form.coordonneesBancaires.numeroCompte" class="form-control" maxlength="11" /></div>
          <div class="col-md-2"><label class="form-label" for="co-cle">Clé</label><input id="co-cle" v-model="form.coordonneesBancaires.cle" class="form-control" maxlength="2" inputmode="numeric" /></div>
          <div class="col-12"><label class="form-label" for="co-iban">IBAN (facultatif)</label><input id="co-iban" v-model="form.coordonneesBancaires.iban" class="form-control" maxlength="34" /></div>
        </div>
      </fieldset>
      <div class="d-flex gap-2">
        <button class="btn btn-primary" type="submit" :disabled="saving">{{ saving ? 'Enregistrement…' : 'Enregistrer' }}</button>
        <button class="btn btn-outline-secondary" type="button" :disabled="saving" @click="edition = false">Annuler</button>
      </div>
    </form>
  </div></section>
</template>
