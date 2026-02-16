package org.digitaltwin.automaton.str;

import org.digitaltwin.automaton.model.Automaton;
import org.digitaltwin.automaton.model.Transition;
import org.digitaltwin.automaton.parser.AutomatonParser;
import org.digitaltwin.automaton.semantics.Configuration;

import java.io.File;
import java.util.*;

/**
 * Demonstrates the STR interface.
 *
 */
public class STRDemo {

    public static void main(String[] args) throws Exception {

        // ── Parse the automaton model ──
        Automaton automaton;
        if (args.length > 0) {
            automaton = AutomatonParser.parse(new File(args[0]));
        } else {
            automaton = AutomatonParser.parseResource("automaton.json");
        }
        System.out.println("Automaton: " + automaton.getName());
        System.out.println();

        // ── Create the STR ──
        // The STR is a passive bridge. It does NOT loop.
        // The caller (this demo, or the Inclusion engine) decides what to do.
        ISemanticTransitionRelation<Configuration, Transition> str = new AutomatonSTR(automaton);

        // ── Step 1: Get initial configurations ──
        Set<Configuration> initials = str.initial();
        System.out.println("=== initial() ===");
        System.out.println("  " + initials);
        System.out.println();

        // Our automaton is deterministic: exactly one initial config
        Configuration current = initials.iterator().next();

        // ── Step 2: Caller-driven exploration ──
        // This is what the Inclusion engine does:
        //   - call actions() to see what's available
        //   - MATCH with trace data to decide which action
        //   - call execute() to advance
        System.out.println("=== Caller-driven trace ===");
        System.out.println();

        Set<String> visited = new HashSet<>();
        visited.add(current.toString());
        int step = 0;

        while (step < 50) {
            step++;

            // Ask the STR: what actions are available?
            Set<Transition> enabled = str.actions(current);

            System.out.println("Step " + step + ": " + current.currentState());
            System.out.println("  config   = " + current);
            System.out.println("  enabled  = " + enabled.stream()
                    .map(Transition::getName).toList());

            if (enabled.isEmpty()) {
                System.out.println("  => DEADLOCK (no enabled actions)");
                break;
            }

            // The CALLER decides which action to fire.
            // In the Inclusion engine: this would be matched against trace data.
            // Here: we just pick the first one.
            Transition chosen = enabled.iterator().next();
            System.out.println("  chosen   = " + chosen.getName());

            // Ask the STR to execute
            Set<Configuration> results = str.execute(current, chosen);

            // Our automaton is deterministic: exactly one result
            Configuration next = results.iterator().next();
            System.out.println("  result   = " + next);
            System.out.println();

            // Loop detection (caller's responsibility, NOT the STR's)
            if (visited.contains(next.toString())) {
                System.out.println("  => LOOP detected by caller. Stopping.");
                break;
            }
            visited.add(next.toString());

            current = next;
        }

        System.out.println();
        System.out.println("=== Final ===");
        System.out.println("  " + current);
        System.out.println("  Steps: " + step);
    }
}
