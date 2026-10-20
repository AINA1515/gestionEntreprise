
package mg.rh.ConceptionLogicielRH;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @GetMapping("/login")
    public String afficherPageConnexion() {
        return "login";
    }

    @GetMapping("/manager/dashboard")
    public String afficherTableauDeBordManager(
            @RequestParam(defaultValue = "accueil") String vue) {

        if ("nouvelle-demande".equals(vue)) {
            return "manager-demande";
        }


        if ("mes-demandes".equals(vue)) {
            return "manager-mes-demandes";
        }

        return "manager-dashboard";
    }


    
    @GetMapping("/candidat/cv")
    public String afficherAccueilCandidat() {
        return "candidat-cv";
    }

        @GetMapping("/candidat/cv/creer")
    public String afficherFormulaireCV() {
        return "candidat-cv-creer";
    }

        @GetMapping("/candidat/offres")
    public String afficherOffresEmploi() {
        return "candidat-offres";
    }

        @GetMapping("/candidat/candidature")
    public String afficherFormulaireCandidature() {
        return "candidat-candidature";
    }

        @GetMapping("/rh/candidatures")
    public String afficherCandidaturesRH() {
        return "rh-candidatures";
    }

    @GetMapping("/rh/decision")
    public String afficherDecisionRecrutement() {
        return "rh-decision";
    }

    @GetMapping("/rh/confirmation")
    public String afficherConfirmationRecrutement() {
        return "rh-confirmation";
    }

}
