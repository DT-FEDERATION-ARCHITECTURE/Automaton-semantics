package org.digitaltwin.automaton.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Root POJO for the automaton metamodel.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Automaton {

    private String name;
    private Space space;
    private InitialConfig initial;
    private List<Transition> transitions;

    public Automaton() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Space getSpace() { return space; }
    public void setSpace(Space space) { this.space = space; }

    public InitialConfig getInitial() { return initial; }
    public void setInitial(InitialConfig initial) { this.initial = initial; }

    public List<Transition> getTransitions() { return transitions; }
    public void setTransitions(List<Transition> transitions) { this.transitions = transitions; }

    /** All transitions leaving from a given state. */
    public List<Transition> transitionsFrom(String state) {
        return transitions.stream()
                .filter(t -> t.getFrom().equals(state))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Automaton{name=" + name + ", states=" + space.getStates()
                + ", transitions=" + transitions.size() + "}";
    }
}
