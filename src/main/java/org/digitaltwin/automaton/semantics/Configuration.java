package org.digitaltwin.automaton.semantics;

/**
 * Immutable configuration: (currentState, {N, V})
 *
 * Every transition produces a NEW configuration.
 */
public final class Configuration {

    private final String currentState;
    private final Valuation valuation;

    public Configuration(String currentState, Valuation valuation) {
        this.currentState = currentState;
        this.valuation = valuation;
    }

    public String currentState() { return currentState; }

    public Valuation valuation() { return valuation; }

    /** New config: different state, copy of valuation. */
    public Configuration withState(String newState) {
        return new Configuration(newState, valuation.copy());
    }

    /** New config: same state, new valuation. */
    public Configuration withValuation(Valuation newVal) {
        return new Configuration(currentState, newVal);
    }

    /** New config: different state and new valuation. */
    public Configuration withStateAndValuation(String newState, Valuation newVal) {
        return new Configuration(newState, newVal);
    }

    @Override
    public String toString() {
        return "(" + currentState + ", " + valuation + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Configuration c)) return false;
        return currentState.equals(c.currentState) && valuation.equals(c.valuation);
    }

    @Override
    public int hashCode() {
        return 31 * currentState.hashCode() + valuation.hashCode();
    }
}
