package org.digitaltwin.automaton.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.digitaltwin.automaton.model.*;

import java.io.*;
import java.util.*;

/**
 * Parses the automaton JSON into POJOs and validates structure.
 */
public class AutomatonParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Set<String> VALID_TYPES = Set.of("int", "float", "bool", "string");

    public static Automaton parse(File jsonFile) throws IOException {
        Automaton a = MAPPER.readValue(jsonFile, Automaton.class);
        validate(a);
        return a;
    }

    public static Automaton parse(String jsonString) throws IOException {
        Automaton a = MAPPER.readValue(jsonString, Automaton.class);
        validate(a);
        return a;
    }

    public static Automaton parseResource(String resourceName) throws IOException {
        try (InputStream is = AutomatonParser.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) throw new IOException("Resource not found: " + resourceName);
            Automaton a = MAPPER.readValue(is, Automaton.class);
            validate(a);
            return a;
        }
    }

    private static void validate(Automaton a) {
        List<String> errors = new ArrayList<>();

        if (a.getName() == null || a.getName().isBlank())
            errors.add("Automaton name is missing");
        if (a.getSpace() == null) {
            errors.add("Space is missing");
            throwIfErrors(errors);
        }
        if (a.getSpace().getStates() == null || a.getSpace().getStates().isEmpty())
            errors.add("No states declared");
        if (a.getInitial() == null)
            errors.add("Initial config is missing");
        if (a.getTransitions() == null)
            errors.add("Transitions list is missing");
        throwIfErrors(errors);

        Set<String> states = new HashSet<>(a.getSpace().getStates());

        // Initial state exists
        if (!states.contains(a.getInitial().getState()))
            errors.add("Initial state '" + a.getInitial().getState() + "' not declared");

        // Variable types valid
        for (Variable v : a.getSpace().getVariables()) {
            if (!VALID_TYPES.contains(v.getType()))
                errors.add("Variable '" + v.getName() + "' has unknown type: " + v.getType());
        }

        // Transitions reference valid states
        for (Transition t : a.getTransitions()) {
            if (!states.contains(t.getFrom()))
                errors.add("Transition '" + t.getName() + "': from '" + t.getFrom() + "' not declared");
            if (!states.contains(t.getTo()))
                errors.add("Transition '" + t.getName() + "': to '" + t.getTo() + "' not declared");
        }

        throwIfErrors(errors);
    }

    private static void throwIfErrors(List<String> errors) {
        if (!errors.isEmpty())
            throw new IllegalArgumentException("Validation failed:\n  - " + String.join("\n  - ", errors));
    }
}
