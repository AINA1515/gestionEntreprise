package com.itu.gestionRH.controller.recruitment;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EvaluationCandidatController {

    @GetMapping("/rh/decision")
    public String decision() {
        return "recruitment/rh-decision";
    }
}
