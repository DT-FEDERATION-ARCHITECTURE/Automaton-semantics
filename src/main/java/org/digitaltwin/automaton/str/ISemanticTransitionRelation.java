package org.digitaltwin.automaton.str;

import java.util.Set;


public interface ISemanticTransitionRelation<C, A> {

    Set<C> initial();

    Set<A> actions(C config);

    Set<C> execute(C config, A action);
}
