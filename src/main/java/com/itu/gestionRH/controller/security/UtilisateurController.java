package com.itu.gestionRH.controller.security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UtilisateurController {

    @GetMapping("/")
    public String accueil() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "security/login";
    }

}
