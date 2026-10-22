package com.itu.gestionRH.workflow;

import com.itu.gestionRH.common.BusinessRuleException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.springframework.context.ApplicationEventPublisher;

/** Lightweight enum state machine with per-transition authorization, guard and effect. */
public final class StateMachine<E, S extends Enum<S>> {
    public record TransitionRequest<E, S extends Enum<S>>(E entite, S avant, S apres,
                                                            String action, WorkflowContext contexte) {}
    private record Transition<E, S extends Enum<S>>(S source, String action, S cible,
            Set<String> roles, Set<String> permissions,
            Predicate<TransitionRequest<E, S>> condition, Consumer<TransitionRequest<E, S>> effet,
            String conditionCode, String conditionMessage) {}

    private final String typeEntite;
    private final Function<E, S> lireEtat;
    private final BiConsumer<E, S> ecrireEtat;
    private final Function<E, Long> lireId;
    private final ApplicationEventPublisher publisher;
    private final List<Transition<E, S>> transitions;

    private StateMachine(String typeEntite, Function<E, S> lireEtat, BiConsumer<E, S> ecrireEtat,
                         Function<E, Long> lireId, ApplicationEventPublisher publisher,
                         List<Transition<E, S>> transitions) {
        this.typeEntite = typeEntite;
        this.lireEtat = lireEtat;
        this.ecrireEtat = ecrireEtat;
        this.lireId = lireId;
        this.publisher = publisher;
        this.transitions = List.copyOf(transitions);
    }

    public WorkflowTransitionEvent transition(E entite, String action, WorkflowContext contexte) {
        if (entite == null || contexte == null) throw new IllegalArgumentException("Entité et contexte requis.");
        S avant = lireEtat.apply(entite);
        List<Transition<E, S>> candidates = transitions.stream()
                .filter(t -> t.action().equals(action) && (t.source() == null || t.source() == avant))
                .toList();
        if (candidates.isEmpty()) throw new BusinessRuleException("WORKFLOW_TRANSITION_INTERDITE",
                "L'action « " + action + " » n'est pas autorisée depuis l'état " + avant + ".");
        List<Transition<E, S>> accessibles = candidates.stream()
                .filter(t -> contexte.hasAccess(t.roles(), t.permissions())).toList();
        if (accessibles.isEmpty()) {
            throw new BusinessRuleException("WORKFLOW_PERMISSION_REFUSEE", "Vous n'avez pas le rôle ou la permission requis.");
        }
        Transition<E, S> transition = null;
        for (Transition<E, S> candidate : accessibles) {
            TransitionRequest<E, S> candidateRequest = new TransitionRequest<>(entite, avant, candidate.cible(), action, contexte);
            if (candidate.condition().test(candidateRequest)) { transition = candidate; break; }
        }
        if (transition == null) {
            Transition<E, S> failed = accessibles.get(0);
            throw new BusinessRuleException(failed.conditionCode(), failed.conditionMessage());
        }
        TransitionRequest<E, S> request = new TransitionRequest<>(entite, avant, transition.cible(), action, contexte);
        ecrireEtat.accept(entite, transition.cible());
        transition.effet().accept(request);
        WorkflowTransitionEvent event = new WorkflowTransitionEvent(typeEntite, lireId.apply(entite),
                avant.name(), transition.cible().name(), contexte.acteur(), Instant.now(), contexte.commentaire());
        publisher.publishEvent(event);
        return event;
    }

    /** Actions are filtered by source state and authority; request-specific guards are checked at transition time. */
    public List<String> actionsDisponibles(E entite, WorkflowContext contexte) {
        S etat = lireEtat.apply(entite);
        Set<String> actions = new LinkedHashSet<>();
        transitions.stream().filter(t -> t.source() == null || t.source() == etat)
                .filter(t -> contexte.hasAccess(t.roles(), t.permissions()))
                .filter(t -> t.condition().test(new TransitionRequest<>(entite, etat, t.cible(), t.action(), contexte)))
                .forEach(t -> actions.add(t.action()));
        return List.copyOf(actions);
    }

    public static <E, S extends Enum<S>> Builder<E, S> builder(String typeEntite,
            Function<E, S> lireEtat, BiConsumer<E, S> ecrireEtat, Function<E, Long> lireId,
            ApplicationEventPublisher publisher) {
        return new Builder<>(typeEntite, lireEtat, ecrireEtat, lireId, publisher);
    }

    public static final class Builder<E, S extends Enum<S>> {
        private final String typeEntite;
        private final Function<E, S> lireEtat;
        private final BiConsumer<E, S> ecrireEtat;
        private final Function<E, Long> lireId;
        private final ApplicationEventPublisher publisher;
        private final List<Transition<E, S>> transitions = new ArrayList<>();

        private Builder(String typeEntite, Function<E, S> lireEtat, BiConsumer<E, S> ecrireEtat,
                        Function<E, Long> lireId, ApplicationEventPublisher publisher) {
            this.typeEntite = typeEntite; this.lireEtat = lireEtat; this.ecrireEtat = ecrireEtat;
            this.lireId = lireId; this.publisher = publisher;
        }
        public Builder<E, S> transition(S source, String action, S cible, Set<String> roles,
                Set<String> permissions, Predicate<TransitionRequest<E, S>> condition,
                Consumer<TransitionRequest<E, S>> effet, String conditionCode, String conditionMessage) {
            transitions.add(new Transition<>(source, action, cible, Set.copyOf(roles), Set.copyOf(permissions),
                    condition, effet, conditionCode, conditionMessage));
            return this;
        }
        public Builder<E, S> transition(S source, String action, S cible, String role) {
            return transition(source, action, cible, Set.of(), Set.of(role), r -> true, r -> {},
                    "WORKFLOW_PRECONDITION_REFUSEE", "La condition métier n'est pas satisfaite.");
        }
        public StateMachine<E, S> build() {
            return new StateMachine<>(typeEntite, lireEtat, ecrireEtat, lireId, publisher, transitions);
        }
    }
}
