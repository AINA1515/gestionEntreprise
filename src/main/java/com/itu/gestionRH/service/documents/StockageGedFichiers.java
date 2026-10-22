package com.itu.gestionRH.service.documents;

import com.itu.gestionRH.entity.enums.DocumentEntite;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StockageGedFichiers implements StockageGed {
    private final Path root;
    public StockageGedFichiers(@Value("${app.ged.root:./storage/ged}") String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    @Override
    public String stocker(String entite, Long entiteId, byte[] contenu) throws IOException {
        DocumentEntite rattachement = DocumentEntite.valueOf(entite);
        if (entiteId == null || entiteId <= 0) throw new IllegalArgumentException("Identifiant d'entité invalide.");
        Path rootPath = root;
        Files.createDirectories(rootPath);
        rootPath = rootPath.toRealPath();
        Path directory = rootPath.resolve(rattachement.name()).resolve(entiteId.toString()).normalize();
        if (!directory.startsWith(rootPath)) throw new SecurityException("Chemin de stockage invalide.");
        Files.createDirectories(directory);
        Path realDirectory = directory.toRealPath();
        if (!realDirectory.startsWith(rootPath)) throw new SecurityException("Le répertoire de stockage sort de la GED.");
        String key = rattachement.name() + "/" + entiteId + "/" + UUID.randomUUID();
        Path target = resolve(key, rootPath);
        Files.write(target, contenu, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        return key;
    }

    @Override
    public InputStream ouvrir(String cle) throws IOException {
        Files.createDirectories(root);
        Path realRoot = root.toRealPath();
        Path target = resolve(cle, realRoot);
        if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) throw new IOException("Fichier GED introuvable.");
        Path realTarget = target.toRealPath();
        if (!realTarget.startsWith(realRoot)) throw new SecurityException("Chemin de stockage invalide.");
        return Files.newInputStream(realTarget);
    }

    private Path resolve(String key, Path realRoot) {
        if (key == null || !key.matches("(CANDIDAT|CANDIDATURE|EMPLOYE|CONTRAT|ONBOARDING_TACHE|POSTE|PROPOSITION_EMBAUCHE)/[1-9][0-9]*/[0-9a-fA-F-]{36}"))
            throw new SecurityException("Clé de stockage invalide.");
        Path resolved = realRoot.resolve(key).normalize();
        if (!resolved.startsWith(realRoot)) throw new SecurityException("Chemin de stockage invalide.");
        return resolved;
    }
}
