package org.digitaltwin.automaton.str;

import org.digitaltwin.automaton.model.Automaton;
import org.digitaltwin.automaton.model.Transition;
import org.digitaltwin.automaton.semantics.*;

import java.util.*;
import java.util.stream.Collectors;


public class AutomatonSTR implements ISemanticTransitionRelation<Configuration, Transition> {

    private final Automaton automaton;


    public AutomatonSTR(Automaton automaton) {
        this.automaton = Objects.requireNonNull(automaton, "automaton must not be null");
    }

    /*
     * @return singleton set containing the initial configuration
     */
    @Override
    public Set<Configuration> initial() {
        Valuation initVal = Valuation.fromMap(
                automaton.getInitial().getVariableValues(),
                automaton.getSpace().getVariables()
        );
        Configuration c0 = new Configuration(automaton.getInitial().getState(), initVal);
        return Set.of(c0);
    }


    @Override
    public Set<Transition> actions(Configuration config) {
        return automaton.transitionsFrom(config.currentState()).stream()
                .filter(t -> GuardEvaluator.evaluate(t.getGuard(), config.valuation()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }


    @Override
    public Set<Configuration> execute(Configuration config, Transition action) {
        // Verify the action is enabled
        if (!GuardEvaluator.evaluate(action.getGuard(), config.valuation())) {
            throw new IllegalArgumentException(
                    "Transition '" + action.getName() + "' is not enabled in " + config);
        }

        // Execute the action: evaluate assignments, build new valuation
        Valuation newVal = ActionEvaluator.execute(action.getAction(), config.valuation());

        // Build new configuration with target state
        Configuration target = new Configuration(action.getTo(), newVal);

        return Set.of(target);
    }


    public Automaton getAutomaton() {
        return automaton;
    }
}
