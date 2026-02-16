package org.digitaltwin.automaton.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Variable declaration: { "name": "x", "type": "int", "initialValue": 0 }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Variable {

    private String name;
    private String type;
    private Object initialValue;

    public Variable() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Object getInitialValue() { return initialValue; }
    public void setInitialValue(Object initialValue) { this.initialValue = initialValue; }

    @Override
    public String toString() {
        return name + ":" + type + "=" + initialValue;
    }
}
