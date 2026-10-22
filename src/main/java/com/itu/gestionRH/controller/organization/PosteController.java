package com.itu.gestionRH.controller.organization;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itu.gestionRH.repository.organization.PosteRepository;

@RestController
@RequestMapping("/api/postes")
public class PosteController {

    public record PosteResponse(Long id, String codePoste, String intitule,
                                Long departementId, String departement) {}

    private final PosteRepository posteRepository;

    public PosteController(PosteRepository posteRepository) {
        this.posteRepository = posteRepository;
    }

    /** Liste des postes, filtrable par departement (?departementId=). */
    @GetMapping
    @Transactional(readOnly = true)
    public List<PosteResponse> lister(@RequestParam(required = false) Long departementId) {
        return posteRepository.findAll().stream()
                .filter(p -> departementId == null
                        || (p.getDepartement() != null && departementId.equals(p.getDepartement().getId())))
                .map(p -> new PosteResponse(
                        p.getId(), p.getCodePoste(), p.getIntitule(),
                        p.getDepartement() != null ? p.getDepartement().getId() : null,
                        p.getDepartement() != null ? p.getDepartement().getLibelle() : null))
                .toList();
    }
}
