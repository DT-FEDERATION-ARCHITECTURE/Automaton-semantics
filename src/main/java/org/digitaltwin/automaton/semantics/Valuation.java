package org.digitaltwin.automaton.semantics;

import org.digitaltwin.automaton.model.Variable;

import java.util.*;
import java.util.stream.Collectors;


 // Variable bindings: {(name, value), ...}

  public class Valuation {

    private final LinkedHashMap<String, Object> bindings;

    private Valuation(LinkedHashMap<String, Object> bindings) {
        this.bindings = bindings;
    }


     //Build valuation from variable declarations.

    public static Valuation fromDeclarations(List<Variable> variables) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (Variable v : variables) {
            map.put(v.getName(), castToType(v.getInitialValue(), v.getType()));
        }
        return new Valuation(map);
    }


     // Build valuation from an explicit map (e.g., initial config values).

    public static Valuation fromMap(Map<String, Object> values, List<Variable> variables) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (Variable v : variables) {
            Object raw = values.getOrDefault(v.getName(), v.getInitialValue());
            map.put(v.getName(), castToType(raw, v.getType()));
        }
        return new Valuation(map);
    }

    public Object get(String name) { return bindings.get(name); }

    public void set(String name, Object value) { bindings.put(name, value); }

    public Set<Map.Entry<String, Object>> entries() {
        return Collections.unmodifiableSet(bindings.entrySet());
    }

    public Valuation copy() {
        return new Valuation(new LinkedHashMap<>(this.bindings));
    }

    // Injects external input values into a COPY of this valuation.

    public Valuation inject(Map<String, Object> inputValues) {
        Valuation result = this.copy();
        if (inputValues == null || inputValues.isEmpty()) return result;

        for (Map.Entry<String, Object> input : inputValues.entrySet()) {
            String inputKey = input.getKey();

            // Exact match first
            if (result.bindings.containsKey(inputKey)) {
                result.bindings.put(inputKey, input.getValue());
                continue;
            }

            // Case-insensitive fallback
            String inputNorm = inputKey.replaceAll("\\s+", "");
            for (String varName : result.bindings.keySet()) {
                if (varName.replaceAll("\\s+", "").equalsIgnoreCase(inputNorm)) {
                    result.bindings.put(varName, input.getValue());
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns the bindings as a plain Map.
     * Used to build AutomatonOutput.
     */

    public Map<String, Object> toMap() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(bindings));
    }

    private static Object castToType(Object raw, String type) {
        if (raw == null) return defaultFor(type);
        return switch (type) {
            case "int"    -> (raw instanceof Number n) ? n.intValue() : Integer.parseInt(raw.toString());
            case "float"  -> (raw instanceof Number n) ? n.doubleValue() : Double.parseDouble(raw.toString());
            case "bool"   -> (raw instanceof Boolean b) ? b : Boolean.parseBoolean(raw.toString());
            case "string" -> raw.toString();
            default       -> raw;
        };
    }

    private static Object defaultFor(String type) {
        return switch (type) {
            case "int"    -> 0;
            case "float"  -> 0.0;
            case "bool"   -> false;
            case "string" -> "";
            default       -> 0;
        };
    }

    @Override
    public String toString() {
        return "{" + bindings.entrySet().stream()
                .map(e -> "(" + e.getKey() + ", " + e.getValue() + ")")
                .collect(Collectors.joining(", ")) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Valuation v)) return false;
        return Objects.equals(bindings, v.bindings);
    }

    @Override
    public int hashCode() { return bindings.hashCode(); }
}