package com.itu.gestionRH.service.documents;

import org.springframework.stereotype.Component;

/** Implémentation neutre à remplacer par un scanner réel en environnement équipé. */
@Component
public class AntivirusScannerInactif implements AntivirusScanner {
    @Override public void verifier(byte[] contenu) { }
}
