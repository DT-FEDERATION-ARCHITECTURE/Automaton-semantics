package org.digitaltwin.automaton;

import org.digitaltwin.automaton.model.*;
import org.digitaltwin.automaton.parser.AutomatonParser;
import org.digitaltwin.automaton.semantics.*;

import java.io.File;
import java.util.*;

/**
 * Automaton Semantics Runner.
 *
 *   mvn compile exec:java -Dexec.args="path/to/automaton.json"
 *
 * Loads the JSON, shows POJOs, then auto-traces through all configurations.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // ── LOAD ──
        Automaton automaton;
        if (args.length > 0) {
            System.out.println(">> Loading: " + args[0] + "\n");
            automaton = AutomatonParser.parse(new File(args[0]));
        } else {
            System.out.println(">> No path given. Using default automaton.json\n");
            automaton = AutomatonParser.parseResource("automaton.json");
        }

        // ── SHOW PARSED POJOs ──
        printSection("1. PARSED POJOs");

        System.out.println("  Automaton    : " + automaton.getName());
        System.out.println("  States       : " + automaton.getSpace().getStates());
        System.out.println("  Variables    :");
        for (Variable v : automaton.getSpace().getVariables()) {
            System.out.println("    " + v.getName() + " : " + v.getType() + " = " + v.getInitialValue());
        }
        System.out.println("  Initial state: " + automaton.getInitial().getState());
        System.out.println("  Initial vals : " + automaton.getInitial().getVariableValues());
        System.out.println("  Transitions  :");
        for (Transition t : automaton.getTransitions()) {
            System.out.println("    " + t);
        }

        // ── BUILD INITIAL CONFIGURATION ──
        printSection("2. INITIAL CONFIGURATION");

        Valuation initVal = Valuation.fromMap(
                automaton.getInitial().getVariableValues(),
                automaton.getSpace().getVariables()
        );
        Configuration config = new Configuration(automaton.getInitial().getState(), initVal);

        System.out.println("  config = " + config);

        // ── AUTOMATIC TRACE ──
        printSection("3. AUTOMATIC TRACE");

        Set<String> visited = new HashSet<>();
        int step = 0;
        int maxSteps = 100;

        while (step < maxSteps) {
            step++;
            System.out.println("  Step " + step);
            System.out.println("    state      = " + config.currentState());
            System.out.println("    valuation  = " + config.valuation());

            // Find enabled transitions from current state
            List<Transition> outgoing = automaton.transitionsFrom(config.currentState());
            List<Transition> enabled = new ArrayList<>();
            for (Transition t : outgoing) {
                boolean result = GuardEvaluator.evaluate(t.getGuard(), config.valuation());
                System.out.println("    eval guard " + t.getName() + ": \"" + t.getGuard() + "\" => " + result);
                if (result) enabled.add(t);
            }

            if (enabled.isEmpty()) {
                System.out.println("    => No transitions enabled. STOPPED.");
                break;
            }

            // Fire first enabled transition
            Transition fired = enabled.get(0);
            if (enabled.size() > 1) {
                System.out.println("    => " + enabled.size() + " transitions enabled: "
                        + enabled.stream().map(Transition::getName).toList());
                System.out.println("    => Firing first: " + fired.getName());
            }

            // Execute action
            Valuation newVal = ActionEvaluator.execute(fired.getAction(), config.valuation());

            // Build new config
            Configuration newConfig = new Configuration(fired.getTo(), newVal);

            System.out.println("    FIRE " + fired.getName() + ": " + config.currentState() + " -> " + fired.getTo());
            if (fired.hasAction()) {
                System.out.println("    action     = " + fired.getAction());
            }
            System.out.println("    new config = " + newConfig);
            System.out.println();

            // Loop detection
            String key = newConfig.toString();
            if (visited.contains(key)) {
                System.out.println("    => LOOP detected: already visited " + newConfig + ". STOPPED.");
                break;
            }
            visited.add(key);

            config = newConfig;
        }

        if (step >= maxSteps) {
            System.out.println("    => Max steps (" + maxSteps + ") reached. STOPPED.");
        }

        // ── FINAL ──
        printSection("4. FINAL RESULT");
        System.out.println("  final config = " + config);
        System.out.println("  state        = " + config.currentState());
        System.out.println("  valuation    = " + config.valuation());
        System.out.println("  steps taken  = " + step);
    }

    private static void printSection(String title) {
        System.out.println();
        System.out.println("  ============================================");
        System.out.println("  " + title);
        System.out.println("  ============================================");
        System.out.println();
    }
}
