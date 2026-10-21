package com.itu.gestionRH.controller.hiring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PropositionEmbaucheController {

    @GetMapping("/rh/confirmation")
    public String confirmation() {
        return "hiring/rh-confirmation";
    }
}
