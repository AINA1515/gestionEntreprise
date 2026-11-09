<script
    setup>    import { onMounted, ref } from 'vue'; import { tempsTravailService } from '@/services/tempsTravailService'; import { employeService } from '@/services/employeService'; const employe = ref(null), creneaux = ref([]), erreur = ref(''), commentaires = ref({}), heuresSup = ref({}); async function load() { if (employe.value) creneaux.value = await tempsTravailService.creneaux(employe.value.id, new Date().toISOString().slice(0, 8) + '01T00:00:00', new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0, 23, 59).toISOString().slice(0, 19)) } async function declarer(c, s) { const h = Number(heuresSup.value[c.id] || 0); if (s === 'TERMINE_HEURES_SUP' && h <= 0) { erreur.value = 'Indiquez les heures supplémentaires.'; return } try { Object.assign(c, await tempsTravailService.declarerCreneau(c.id, s, commentaires.value[c.id] || '', Math.round(h * 60))) } catch (e) { erreur.value = e.message || 'Déclaration impossible.' } } onMounted(async () => { try { employe.value = await employeService.moi(); await load() } catch (e) { erreur.value = e.message || 'Calendrier indisponible.' } })</script>
<template>
    <div class="container-fluid py-4">
        <h1 class="h3">Mon temps de travail</h1>
        <p v-if="erreur" class="alert alert-danger">{{ erreur }}</p>
        <div class="row g-3">
            <article v-for="c in creneaux" :key="c.id" class="col-md-6 col-xl-4">
                <div class="card card-body h-100"><strong>{{ c.libelle || 'Travail' }}</strong>
                    <div>{{ new Date(c.dateDebut).toLocaleString('fr-FR') }} → {{ new
                        Date(c.dateFin).toLocaleTimeString('fr-FR', { hour:'2-digit',minute:'2-digit'})}}</div><span
                        v-if="c.statutValidation === 'VALIDEE'" class="badge text-bg-success mt-2">Déjà terminé et
                        validé</span><span v-else-if="c.statutValidation === 'REFUSEE' || c.statutEmploye === 'A_REFAIRE'"
                        class="badge text-bg-danger mt-2">Refusé — à refaire</span><span v-else
                        class="badge text-bg-warning mt-2">{{ c.statutEmploye || 'NON_COMMENCE' }}</span><template
                        v-if="c.statutValidation !== 'VALIDEE'"><textarea v-model="commentaires[c.id]"
                            class="form-control mt-2" placeholder="Commentaire"></textarea><label
                            class="form-label mt-2">Heures supplémentaires</label><input v-model="heuresSup[c.id]"
                            type="number" min="0" step="0.5" class="form-control">
                        <div class="btn-group mt-2"><button class="btn btn-success"
                                @click="declarer(c, 'TERMINE')">Terminé</button><button class="btn btn-warning"
                                @click="declarer(c, 'TERMINE_HEURES_SUP')">Terminé + heures sup.</button><button
                                class="btn btn-secondary" @click="declarer(c, 'NON_TERMINE')">Pas terminé</button></div>
                    </template>
                </div>
            </article>
            <p v-if="!creneaux.length">Aucun créneau ce mois-ci.</p>
        </div>
    </div>
</template>
