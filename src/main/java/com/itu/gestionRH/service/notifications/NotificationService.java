package com.itu.gestionRH.service.notifications;

import com.itu.gestionRH.common.NotFoundException;
import com.itu.gestionRH.common.PageResponse;
import com.itu.gestionRH.entity.enums.NotificationStatut;
import com.itu.gestionRH.entity.notifications.Notification;
import com.itu.gestionRH.entity.security.Utilisateur;
import com.itu.gestionRH.repository.notifications.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final Pattern VARIABLE_EXACT = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*}}" );
    private final NotificationRepository repository;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean mailEnabled;
    private final String from;

    public NotificationService(NotificationRepository repository, ObjectProvider<JavaMailSender> mailSender,
            @Value("${app.notifications.mail.enabled:false}") boolean mailEnabled,
            @Value("${app.notifications.mail.from:no-reply@gestionrh.local}") String from) {
        this.repository = repository; this.mailSender = mailSender; this.mailEnabled = mailEnabled; this.from = from;
    }

    /** Persiste l'outbox dans la transaction métier courante. */
    @Transactional
    public void programmer(String modele, Collection<Utilisateur> destinataires, Map<String, ?> variables,
            String entite, Long entiteId) {
        String corps = rendre(modele, variables);
        String sujet = sujet(modele);
        if (destinataires == null) return;
        destinataires.stream().filter(u -> u != null && u.getEmail() != null && !u.getEmail().isBlank()).distinct().forEach(u -> {
            Notification n = Notification.nouvelle();
            n.setDestinataire(u); n.setDestinataireEmail(u.getEmail()); n.setSujet(sujet); n.setCorps(corps);
            n.setCodeModele(modele); n.setEntiteRattachement(entite); n.setEntiteId(entiteId);
            n.setStatutEnvoi(NotificationStatut.EN_ATTENTE); n.setNbTentatives(0); n.setDateEnvoiPrevue(LocalDateTime.now()); n.setEstLue(false);
            repository.save(n);
        });
    }

    @Transactional
    public void programmerUneFoisParJour(String modele, Collection<Utilisateur> destinataires, Map<String, ?> variables,
            String entite, Long entiteId) {
        if (destinataires == null) return;
        LocalDateTime depuis = LocalDateTime.now().toLocalDate().atStartOfDay();
        var nouveaux = destinataires.stream().filter(u -> u != null && u.getId() != null)
                .filter(u -> !repository.existsByCodeModeleAndEntiteRattachementAndEntiteIdAndDestinataireIdAndDateCreationAfter(
                        modele, entite, entiteId, u.getId(), depuis)).toList();
        programmer(modele, nouveaux, variables, entite, entiteId);
    }

    @Transactional
    public void envoyerEcheances(int limite) {
        List<Notification> enAttente = repository.verrouillerEnAttente(LocalDateTime.now(), Math.max(1, limite));
        for (Notification notification : enAttente) {
            try {
                envoyer(notification);
                notification.setStatutEnvoi(NotificationStatut.ENVOYEE);
                notification.setDateEnvoiReel(LocalDateTime.now());
                notification.setMessageErreur(null);
            } catch (Exception ex) {
                int tentatives = notification.getNbTentatives() == null ? 1 : notification.getNbTentatives() + 1;
                notification.setNbTentatives(tentatives);
                notification.setMessageErreur(limiter(ex.getMessage(), 1000));
                if (tentatives >= 5) notification.setStatutEnvoi(NotificationStatut.ECHEC);
                else notification.setDateEnvoiPrevue(LocalDateTime.now().plusMinutes(1L << (tentatives - 1)));
                log.warn("Échec d'envoi de notification {} (tentative {})", notification.getId(), tentatives);
            }
        }
        repository.flush();
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationDto> mesNotifications(Long destinataireId, int page, int size) {
        var pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Order.asc("estLue"), Sort.Order.desc("dateCreation")));
        return PageResponse.from(repository.findByDestinataireIdOrderByEstLueAscDateCreationDesc(destinataireId, pageable).map(NotificationService::dto));
    }

    @Transactional(readOnly = true)
    public long nombreNonLues(Long destinataireId) { return repository.countByDestinataireIdAndEstLueFalse(destinataireId); }

    @Transactional
    public void marquerLue(Long id, Long destinataireId) {
        Notification n = repository.findByIdAndDestinataireId(id, destinataireId).orElseThrow(() -> new NotFoundException("Notification introuvable."));
        n.marquerLue();
    }

    @Transactional
    public int marquerToutLu(Long destinataireId) { return repository.marquerToutesLues(destinataireId); }

    private void envoyer(Notification n) throws Exception {
        if (!mailEnabled) { log.info("[notification/dev] à={} sujet={} code={}", n.getDestinataireEmail(), n.getSujet(), n.getCodeModele()); return; }
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) throw new IllegalStateException("Aucun serveur SMTP n'est configuré.");
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
        helper.setFrom(from); helper.setTo(n.getDestinataireEmail()); helper.setSubject(n.getSujet());
        helper.setText(n.getCorps() == null ? "" : n.getCorps(), true);
        sender.send(message);
    }

    private String rendre(String modele, Map<String, ?> variables) {
        if (modele == null || !modele.matches("[A-Z0-9_]{1,50}")) throw new IllegalArgumentException("Code de modèle invalide.");
        try {
            String html = new ClassPathResource("notifications/" + modele + ".html").getContentAsString(StandardCharsets.UTF_8);
            Matcher matcher = VARIABLE_EXACT.matcher(html);
            StringBuffer output = new StringBuffer();
            while (matcher.find()) {
                Object value = variables == null ? null : variables.get(matcher.group(1));
                matcher.appendReplacement(output, Matcher.quoteReplacement(echapper(value == null ? "" : value.toString())));
            }
            matcher.appendTail(output);
            return output.toString();
        } catch (IOException e) { throw new IllegalArgumentException("Modèle de notification inconnu : " + modele, e); }
    }

    private String sujet(String modele) {
        return switch (modele) {
            case "DEMANDE_SOUMISE" -> "Nouvelle demande de recrutement";
            case "DEMANDE_TRANSMISE_DIRECTION" -> "Demande de recrutement à arbitrer";
            case "DEMANDE_VALIDEE" -> "Demande de recrutement validée";
            case "DEMANDE_REFUSEE" -> "Demande de recrutement refusée";
            case "OFFRE_PUBLIEE" -> "Une offre d'emploi est publiée";
            case "CANDIDATURE_RECUE" -> "Votre candidature a bien été reçue";
            case "CANDIDATURE_PRESELECTIONNEE" -> "Candidature présélectionnée";
            case "ENTRETIEN_PLANIFIE" -> "Entretien planifié";
            case "CANDIDATURE_REJETEE" -> "Mise à jour de votre candidature";
            case "PROPOSITION_ENVOYEE" -> "Votre proposition d'embauche";
            case "PROPOSITION_ACCEPTEE" -> "Proposition d'embauche acceptée";
            case "PROPOSITION_REFUSEE" -> "Proposition d'embauche refusée";
            case "EMPLOYE_CREE" -> "Création d'un dossier salarié";
            case "ONBOARDING_TACHE_ASSIGNEE" -> "Une tâche d'intégration vous est assignée";
            case "ONBOARDING_TACHE_EN_RETARD" -> "Une tâche d'intégration est en retard";
            case "CONTRAT_FIN_PROCHE" -> "Échéance prochaine d'un contrat";
            default -> throw new IllegalArgumentException("Modèle de notification non supporté : " + modele);
        };
    }

    private String echapper(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
    private String limiter(String message, int max) { if (message == null) return "Erreur d'envoi SMTP."; return message.length() <= max ? message : message.substring(0, max); }

    public record NotificationDto(Long id, String sujet, String corps, String codeModele, String entiteRattachement,
            Long entiteId, Boolean lue, LocalDateTime dateCreation) {}
    private static NotificationDto dto(Notification n) {
        return new NotificationDto(n.getId(), n.getSujet(), n.getCorps(), n.getCodeModele(), n.getEntiteRattachement(), n.getEntiteId(), n.getEstLue(), n.getDateCreation());
    }
}
