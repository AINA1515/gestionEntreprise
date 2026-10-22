package com.itu.gestionRH.service.recruitment;

import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OffreExpirationScheduler {
    private final OffreEmploiService offres;

    public OffreExpirationScheduler(OffreEmploiService offres) {
        this.offres = offres;
    }

    @Scheduled(cron = "${app.recruitment.offer-expiration.cron:0 5 0 * * *}")
    public void cloturerExpirees() {
        offres.cloturerOffresExpirees(LocalDate.now());
    }
}
