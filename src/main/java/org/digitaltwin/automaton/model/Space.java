package org.digitaltwin.automaton.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * State space: { "states": ["S0","S1"], "variables": [...] }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Space {

    private List<String> states;
    private List<Variable> variables;

    public Space() {}

    public List<String> getStates() { return states; }
    public void setStates(List<String> states) { this.states = states; }

    public List<Variable> getVariables() { return variables; }
    public void setVariables(List<Variable> variables) { this.variables = variables; }

    @Override
    public String toString() {
        return "Space{states=" + states + ", variables=" + variables + "}";
    }
}
