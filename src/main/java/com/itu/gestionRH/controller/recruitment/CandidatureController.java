package com.itu.gestionRH.controller.recruitment;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CandidatureController {

    @GetMapping("/rh/candidatures")
    public String suiviCandidatures() {
        return "recruitment/rh-candidatures";
    }

    @GetMapping("/candidat/candidature")
    public String maCandidature() {
        return "recruitment/candidat-candidature";
    }
}
