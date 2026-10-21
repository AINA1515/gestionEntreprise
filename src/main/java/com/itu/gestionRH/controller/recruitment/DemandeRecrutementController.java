package com.itu.gestionRH.controller.recruitment;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DemandeRecrutementController {

    // Les liens des maquettes utilisent /manager/dashboard?vue=...
    @GetMapping("/manager/dashboard")
    public String dashboard(@RequestParam(required = false) String vue) {
        if ("nouvelle-demande".equals(vue)) {
            return "recruitment/manager-demande";
        }
        if ("mes-demandes".equals(vue)) {
            return "recruitment/manager-mes-demandes";
        }
        return "recruitment/manager-dashboard";
    }
}
