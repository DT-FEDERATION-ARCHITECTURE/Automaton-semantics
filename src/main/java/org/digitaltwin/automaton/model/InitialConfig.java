package org.digitaltwin.automaton.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Initial configuration from the metamodel:
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitialConfig {

    private String state;
    private final Map<String, Object> variableValues = new LinkedHashMap<>();

    public InitialConfig() {}

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    /**
     * Jackson calls this for every JSON field that is NOT "state".
     */
    @JsonAnySetter
    public void setVariableValue(String name, Object value) {
        variableValues.put(name, value);
    }

    public Map<String, Object> getVariableValues() {
        return variableValues;
    }

    @Override
    public String toString() {
        return "Initial{state=" + state + ", values=" + variableValues + "}";
    }
}
