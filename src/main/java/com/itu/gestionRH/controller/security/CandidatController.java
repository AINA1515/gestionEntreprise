package com.itu.gestionRH.controller.security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CandidatController {

    @GetMapping("/candidat/cv")
    public String cv() {
        return "security/candidat-cv";
    }

    @GetMapping("/candidat/cv/creer")
    public String cvCreer() {
        return "security/candidat-cv-creer";
    }
}
