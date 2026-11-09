<script setup>
import { onMounted, ref } from 'vue'
import { employeService } from '@/services/employeService'
import { pointageService } from '@/services/pointageService'
const employe=ref(null),lignes=ref([]),erreur=ref('');const maintenant=new Date();const debut=`${maintenant.getFullYear()}-${String(maintenant.getMonth()+1).padStart(2,'0')}-01`;const fin=new Date(maintenant.getFullYear(),maintenant.getMonth()+1,0).toISOString().slice(0,10)
onMounted(async()=>{try{employe.value=await employeService.moi();lignes.value=await pointageService.lister(employe.value.id,debut,fin)}catch(e){erreur.value=e.response?.data?.message||'Pointages indisponibles.'}})
</script>
<template><div class="container-fluid py-4"><h1 class="h3">Mes pointages</h1><p v-if="erreur" class="alert alert-danger">{{erreur}}</p><section class="card"><div class="table-responsive"><table class="table align-middle mb-0"><thead><tr><th>Date</th><th>Entrée</th><th>Sortie</th><th>Retard</th><th>Heures sup.</th><th>Statut</th></tr></thead><tbody><tr v-if="!lignes.length"><td colspan="6" class="text-center text-body-secondary">Aucun pointage enregistré.</td></tr><tr v-for="p in lignes" :key="p.id"><td>{{p.datePointage}}</td><td>{{p.entreeReelle||'—'}}</td><td>{{p.sortieReelle||'—'}}</td><td>{{p.minutesRetard}} min</td><td>{{p.minutesHeuresSup}} min</td><td><span class="badge" :class="p.statut==='RETARD'?'text-bg-warning':p.statut==='ABSENT'?'text-bg-danger':'text-bg-success'">{{p.statut}}</span></td></tr></tbody></table></div></section></div></template>
