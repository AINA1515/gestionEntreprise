package com.itu.gestionRH.workflow;

import java.time.Instant;

public record WorkflowTransitionEvent(String typeEntite, Long idEntite, String etatAvant,
                                      String etatApres, String acteur, Instant date,
                                      String commentaire) {}
