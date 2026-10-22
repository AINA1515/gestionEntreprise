package com.itu.gestionRH.service.documents;

/** Point d'extension pour brancher un antivirus sans modifier DocumentService. */
public interface AntivirusScanner {
    void verifier(byte[] contenu);
}
