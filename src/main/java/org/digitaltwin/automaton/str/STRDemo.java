package org.digitaltwin.automaton.str;

import org.digitaltwin.automaton.model.Automaton;
import org.digitaltwin.automaton.model.Transition;
import org.digitaltwin.automaton.parser.AutomatonParser;
import org.digitaltwin.automaton.semantics.Configuration;

import java.io.File;
import java.util.*;

/**
 * Demonstrates the Automaton SLI with two input modes:
 *
 * 1. Direct Map input (standalone testing)
 *    AutomatonSTR.withMapInput(automaton)
 *
 * 2. StepCip-like input (simulates membership integration)
 *    new AutomatonSTR<>(automaton, step -> step.current)
 *
 * In production, the membership wires it as:
 *    new AutomatonSTR<>(automaton, step -> step.current().getValues())
 */
public class STRDemo {

    /**
     * Simulates StepCip<M> from gemini3d-trace.
     * In production, this is the real StepCip class.
     * Here we mimic it to show the extractor pattern works.
     */
    record Step(Map<String, Object> last, long deltaTms, Map<String, Object> current) {
        @Override
        public String toString() {
            return "(last=" + last + ", deltaT=" + deltaTms + "ms, current=" + current + ")";
        }
    }

    public static void main(String[] args) throws Exception {

        Automaton automaton;
        if (args.length > 0) {
            automaton = AutomatonParser.parse(new File(args[0]));
        } else {
            automaton = AutomatonParser.parseResource("automaton.json");
        }

        System.out.println("+--[AUTOMATON SLI DEMO]---------------------------------------+");
        System.out.println("|  Automaton  : " + automaton.getName());
        System.out.println("|  States     : " + automaton.getSpace().getStates());
        System.out.println("|  Variables  : " + automaton.getSpace().getVariables());
        System.out.println("+------------------------------------------------------------+");

        // ════════════════════════════════════════════════════════
        // DEMO 1: Direct Map input (standalone testing)
        // ════════════════════════════════════════════════════════
        System.out.println();
        System.out.println("═══ DEMO 1: Direct Map Input (standalone) ═══════════════════");
        System.out.println();

        AutomatonSTR<Map<String, Object>> sli1 = AutomatonSTR.withMapInput(automaton);

        List<Map<String, Object>> measurements = List.of(
                Map.of("x", 0, "y", 0, "z", 0),
                Map.of("x", 10, "y", 10, "z", 0),
                Map.of("x", 15, "y", 7, "z", 0),
                Map.of("x", 13, "y", 9, "z", 0)
        );

        Configuration current = sli1.initial().iterator().next();
        System.out.println("initial() → " + current);
        System.out.println();

        int step = 0;
        for (Map<String, Object> m : measurements) {
            step++;
            System.out.println("── Step " + step + " ──");
            System.out.println("  INPUT  : " + m);
            current = runStep(sli1, m, current);
        }

        System.out.println("FINAL: " + current);

        // ════════════════════════════════════════════════════════
        // DEMO 2: StepCip-like input (simulates membership)
        // ════════════════════════════════════════════════════════
        System.out.println();
        System.out.println("═══ DEMO 2: StepCip-like Input (membership mode) ════════════");
        System.out.println();

        // Extractor: Step → Map<String, Object>
        // Takes current measurement from the step (mi+1)
        AutomatonSTR<Step> sli2 = new AutomatonSTR<>(automaton, s -> s.current());

        List<Step> steps = List.of(
                new Step(Map.of(),                         0,    Map.of("x", 0, "y", 0, "z", 0)),
                new Step(Map.of("x", 0, "y", 0, "z", 0),  5000, Map.of("x", 10, "y", 10, "z", 0)),
                new Step(Map.of("x", 10, "y", 10, "z", 0),5000, Map.of("x", 15, "y", 7, "z", 0)),
                new Step(Map.of("x", 15, "y", 7, "z", 0), 5000, Map.of("x", 13, "y", 9, "z", 0))
        );

        current = sli2.initial().iterator().next();
        System.out.println("initial() → " + current);
        System.out.println();

        step = 0;
        for (Step s : steps) {
            step++;
            System.out.println("── Step " + step + " ──");
            System.out.println("  INPUT  : " + s);
            current = runStep(sli2, s, current);
        }

        System.out.println("FINAL: " + current);
    }

    /**
     * Runs one SLI step: actions → execute → print → return new config.
     * Works with ANY input type thanks to generics.
     */
    private static <I> Configuration runStep(
            ISemanticTransitionRelation<I, AutomatonOutput, Transition, Configuration> sli,
            I input,
            Configuration current) {

        Set<Transition> enabled = sli.actions(input, current);
        System.out.println("  enabled: " + enabled.stream().map(Transition::getName).toList());

        if (enabled.isEmpty()) {
            System.out.println("  => NO TRANSITION. Stopped.");
            System.out.println();
            return current;
        }

        Transition chosen = enabled.iterator().next();
        var results = sli.execute(chosen, input, current);
        var result = results.iterator().next();

        System.out.println("  fired  : " + chosen.getName() + " (" + chosen.getFrom() + " → " + chosen.getTo() + ")");
        System.out.println("  OUTPUT : " + result.output());
        System.out.println();

        return result.configuration();
    }
}