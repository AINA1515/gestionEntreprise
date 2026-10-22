package com.itu.gestionRH.controller.organization;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itu.gestionRH.repository.organization.DepartementRepository;

@RestController
@RequestMapping("/api/departements")
public class DepartementController {

    public record DepartementResponse(Long id, String code, String libelle) {}

    private final DepartementRepository departementRepository;

    public DepartementController(DepartementRepository departementRepository) {
        this.departementRepository = departementRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<DepartementResponse> lister() {
        return departementRepository.findAll().stream()
                .filter(d -> !Boolean.FALSE.equals(d.getEstActif()))
                .map(d -> new DepartementResponse(d.getId(), d.getCode(), d.getLibelle()))
                .toList();
    }
}
