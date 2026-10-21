package com.itu.gestionRH.controller.recruitment;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OffreEmploiController {

    @GetMapping("/candidat/offres")
    public String offres() {
        return "recruitment/candidat-offres";
    }
}
