package com.itu.gestionRH.workflow;

public final class WorkflowRules {
    private WorkflowRules() {}
    public static boolean justificationPresente(WorkflowContext context) {
        return context.commentaire() != null && !context.commentaire().isBlank();
    }
}
