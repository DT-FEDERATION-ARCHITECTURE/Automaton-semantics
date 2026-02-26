package org.digitaltwin.automaton.str;

import org.digitaltwin.automaton.model.Automaton;
import org.digitaltwin.automaton.model.Transition;
import org.digitaltwin.automaton.semantics.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class AutomatonSTR<I>
        implements ISemanticTransitionRelation<I, AutomatonOutput, Transition, Configuration> {

    private final Automaton automaton;
    private final Function<I, Map<String, Object>> inputExtractor;


    public AutomatonSTR(Automaton automaton, Function<I, Map<String, Object>> inputExtractor) {
        this.automaton = Objects.requireNonNull(automaton, "automaton must not be null");
        this.inputExtractor = Objects.requireNonNull(inputExtractor, "inputExtractor must not be null");
    }

    /**
     * Convenience: creates an Automaton SLI where I = Map<String, Object>.
     * The extractor is identity (input is already a map of values).
     */
    public static AutomatonSTR<Map<String, Object>> withMapInput(Automaton automaton) {
        return new AutomatonSTR<>(automaton, input -> input);
    }

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
    public Set<Transition> actions(I input, Configuration configuration) {
        Map<String, Object> values = inputExtractor.apply(input);
        Valuation fed = configuration.valuation().inject(values);

        return automaton.transitionsFrom(configuration.currentState()).stream()
                .filter(t -> GuardEvaluator.evaluate(t.getGuard(), fed))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<ISemanticTransitionRelation.Pair<AutomatonOutput, Configuration>> execute(
            Transition action,
            I input,
            Configuration configuration) {

        Map<String, Object> values = inputExtractor.apply(input);

        // Step 1: Inject input → measured vars get real values
        Valuation fed = configuration.valuation().inject(values);

        // Step 2: Execute action expressions → computed vars updated
        Valuation newVal = ActionEvaluator.execute(action.getAction(), fed);

        // Step 3: Build new config with target state
        Configuration newConfig = new Configuration(action.getTo(), newVal);

        // Step 4: Build output → (currentState, {N,V})
        AutomatonOutput output = new AutomatonOutput(newConfig.currentState(), newVal.toMap());

        return Set.of(new ISemanticTransitionRelation.Pair<>(output, newConfig));
    }

    public Automaton getAutomaton() {
        return automaton;
    }
}