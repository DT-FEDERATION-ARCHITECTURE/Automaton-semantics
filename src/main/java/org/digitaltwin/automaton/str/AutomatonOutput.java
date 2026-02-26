package org.digitaltwin.automaton.str;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Output of the Automaton SLI: (currentState, {N, V}).
 * @param currentState the automaton state after this step (e.g., "S1")
 * @param valuation    variable bindings: {(name, value), ...}
 */
public record AutomatonOutput(String currentState, Map<String, Object> valuation) {

    @Override
    public String toString() {
        String vars = valuation.entrySet().stream()
                .map(e -> "(" + e.getKey() + ", " + e.getValue() + ")")
                .collect(Collectors.joining(", "));
        return "(" + currentState + ", {" + vars + "})";
    }
}