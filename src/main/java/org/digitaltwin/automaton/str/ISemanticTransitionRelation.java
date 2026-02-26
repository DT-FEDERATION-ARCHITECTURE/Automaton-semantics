package org.digitaltwin.automaton.str;

import java.util.Set;


public interface ISemanticTransitionRelation<I, O, A, C> {

    Set<C> initial();

    Set<A> actions(I input, C configuration);

    Set<Pair<O, C>> execute(A action, I input, C configuration);

    record Pair<O, C>(O output, C configuration) {}
}
