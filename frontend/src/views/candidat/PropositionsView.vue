<script setup>
import { onMounted, ref } from 'vue'
import { candidatureService } from '@/services/candidatureService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const propositions = ref([])
const { loading, error, run } = useAsyncAction()
const { formatDate, formatMontant } = useFormatters()
async function charger() { propositions.value = await run(() => candidatureService.mesPropositions()) || [] }
async function repondre(proposition, statut) {
  let commentaire = null
  if (statut === 'REFUSEE') {
    commentaire = window.prompt('Indiquez le motif du refus :')
    if (!commentaire?.trim()) return
  } else if (!window.confirm('Accepter cette proposition d’embauche ?')) return
  const resultat = await run(() => candidatureService.repondreProposition(proposition.id, statut, commentaire))
  if (resultat) await charger()
}
onMounted(charger)
</script>

<template>
  <PageHeader title="Mes propositions d’embauche" :breadcrumb="['Espace candidat', 'Propositions']" subtitle="Consultez les propositions transmises après vos entretiens." />
  <AlertMessage :message="error" />
  <section class="card shadow-sm"><div v-if="loading" class="text-center py-5" role="status"><span class="spinner-border spinner-border-sm me-2"/>Chargement…</div>
    <div v-else-if="!propositions.length" class="card-body text-center text-body-secondary py-5">Vous n’avez pas encore de proposition d’embauche.</div>
    <div v-else class="table-responsive"><table class="table table-hover align-middle mb-0"><thead class="table-light"><tr><th>Offre</th><th>Contrat</th><th>Salaire brut proposé</th><th>Dates du contrat</th><th>Réponse avant le</th><th>Statut</th><th>Dossier salarié</th><th></th></tr></thead>
      <tbody><tr v-for="p in propositions" :key="p.id"><td>{{ p.poste }}</td><td>{{ p.typeContrat }}</td><td>{{ formatMontant(p.salaireBrut) }}</td><td>{{ formatDate(p.dateDebut) }} <span v-if="p.dateFin">au {{ formatDate(p.dateFin) }}</span></td><td>{{ formatDate(p.dateLimiteReponse) }}</td><td>{{ p.statut }}</td><td>{{ p.matricule || 'Créé après acceptation' }}</td>
        <td v-if="p.statut === 'ENVOYEE'" class="text-nowrap"><button class="btn btn-sm btn-success me-2" :disabled="loading" @click="repondre(p, 'ACCEPTEE')">Accepter</button><button class="btn btn-sm btn-outline-danger" :disabled="loading" @click="repondre(p, 'REFUSEE')">Refuser</button></td><td v-else>—</td>
      </tr></tbody></table></div>
  </section>
</template>
