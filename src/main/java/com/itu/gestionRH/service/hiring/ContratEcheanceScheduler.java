package com.itu.gestionRH.service.hiring;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ContratEcheanceScheduler {
    private final ContratSuiviService service;

    public ContratEcheanceScheduler(ContratSuiviService service) { this.service = service; }

    @Scheduled(cron = "${app.contrats.echeances.cron:0 10 0 * * *}")
    public void appliquerEcheances() { service.appliquerEcheances(); }
}
