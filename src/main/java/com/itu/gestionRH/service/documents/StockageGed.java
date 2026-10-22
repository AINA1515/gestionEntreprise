package com.itu.gestionRH.service.documents;

import java.io.IOException;
import java.io.InputStream;

/** Stockage binaire interchangeable (filesystem, S3, MinIO...). */
public interface StockageGed {
    String stocker(String entite, Long entiteId, byte[] contenu) throws IOException;
    InputStream ouvrir(String cle) throws IOException;
}
