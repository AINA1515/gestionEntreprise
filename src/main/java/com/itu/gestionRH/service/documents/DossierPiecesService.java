package com.itu.gestionRH.service.documents;

import com.itu.gestionRH.entity.documents.Document;
import com.itu.gestionRH.entity.enums.DocumentEntite;
import com.itu.gestionRH.entity.enums.DocumentStatut;
import com.itu.gestionRH.entity.enums.DocumentValidation;
import com.itu.gestionRH.entity.enums.TypeDocument;
import com.itu.gestionRH.entity.hiring.Contrat;
import com.itu.gestionRH.repository.documents.DocumentObligatoireRepository;
import com.itu.gestionRH.repository.documents.DocumentRepository;
import com.itu.gestionRH.repository.hiring.ContratRepository;
import com.itu.gestionRH.security.AccessScope;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** État des pièces obligatoires d'un dossier : ce qui est fourni, validé, refusé, expiré ou manquant. */
@Service
@Transactional(readOnly = true)
public class DossierPiecesService {
    public enum PieceStatut { MANQUANT, A_VALIDER, VALIDE, REFUSE, EXPIRE, EXPIRE_BIENTOT }

    public record PieceEtat(Long obligatoireId, TypeDocument typeDocument, String libelle, PieceStatut statut, Long documentId,
            Integer numeroVersion, LocalDate dateExpiration, Long joursAvantExpiration, DocumentValidation statutValidation, String motifRefus) {}

    /** {@code complet} : toutes les pièces sont fournies, validées et en cours de validité. */
    public record EtatPieces(Long employeId, List<PieceEtat> pieces, int manquantes, int aValider, int refusees, int expirees,
            int expirantBientot, boolean complet) {}

    private final DocumentObligatoireRepository obligatoires;
    private final DocumentRepository documents;
    private final ContratRepository contrats;
    private final AccessScope accessScope;

    public DossierPiecesService(DocumentObligatoireRepository obligatoires, DocumentRepository documents,
            ContratRepository contrats, AccessScope accessScope) {
        this.obligatoires = obligatoires; this.documents = documents; this.contrats = contrats; this.accessScope = accessScope;
    }

    public EtatPieces etat(Long employeId) {
        accessScope.requireDossier(employeId);
        List<Contrat> historique = contrats.findByEmployeIdOrderByDateDebutDescIdDesc(employeId);
        var typeContrat = historique.isEmpty() ? null : historique.get(0).getTypeContrat();

        // Pièces actives rattachées au salarié ou à l'un de ses contrats ; une seule par type (la plus récente).
        List<Document> actifs = new ArrayList<>(documents.findByEntiteRattachementAndEntiteIdInAndStatutDocument(
                DocumentEntite.EMPLOYE, List.of(employeId), DocumentStatut.ACTIF));
        if (!historique.isEmpty())
            actifs.addAll(documents.findByEntiteRattachementAndEntiteIdInAndStatutDocument(DocumentEntite.CONTRAT,
                    historique.stream().map(Contrat::getId).toList(), DocumentStatut.ACTIF));
        Map<TypeDocument, Document> parType = new HashMap<>();
        actifs.stream().sorted(Comparator.comparing(Document::getDateCreation, Comparator.nullsFirst(Comparator.naturalOrder())))
                .forEach(d -> parType.put(d.getTypeDocument(), d));

        LocalDate aujourdHui = LocalDate.now();
        List<PieceEtat> pieces = new ArrayList<>();
        for (var o : obligatoires.findByEstActifTrueOrderByOrdreAscIdAsc()) {
            if (o.getTypeContrat() != null && o.getTypeContrat() != typeContrat) continue;
            Document d = parType.get(o.getTypeDocument());
            if (d == null) {
                pieces.add(new PieceEtat(o.getId(), o.getTypeDocument(), o.getLibelle(), PieceStatut.MANQUANT, null, null, null, null, null, null));
                continue;
            }
            Long jours = d.getDateExpiration() == null ? null : ChronoUnit.DAYS.between(aujourdHui, d.getDateExpiration());
            PieceStatut statut;
            if (jours != null && jours < 0) statut = PieceStatut.EXPIRE;
            else if (d.getStatutValidation() == DocumentValidation.REFUSE) statut = PieceStatut.REFUSE;
            else if (d.getStatutValidation() == DocumentValidation.A_VALIDER) statut = PieceStatut.A_VALIDER;
            else if (jours != null && jours <= o.getDelaiAlerteJours()) statut = PieceStatut.EXPIRE_BIENTOT;
            else statut = PieceStatut.VALIDE;
            pieces.add(new PieceEtat(o.getId(), o.getTypeDocument(), o.getLibelle(), statut, d.getId(), d.getNumeroVersion(),
                    d.getDateExpiration(), jours, d.getStatutValidation(), d.getMotifRefus()));
        }
        int manquantes = compter(pieces, PieceStatut.MANQUANT), aValider = compter(pieces, PieceStatut.A_VALIDER),
                refusees = compter(pieces, PieceStatut.REFUSE), expirees = compter(pieces, PieceStatut.EXPIRE),
                bientot = compter(pieces, PieceStatut.EXPIRE_BIENTOT);
        return new EtatPieces(employeId, pieces, manquantes, aValider, refusees, expirees, bientot,
                manquantes + aValider + refusees + expirees == 0);
    }

    private static int compter(List<PieceEtat> pieces, PieceStatut statut) {
        return (int) pieces.stream().filter(p -> p.statut() == statut).count();
    }
}
