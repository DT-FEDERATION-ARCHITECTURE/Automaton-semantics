package org.digitaltwin.automaton.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A transition: { "name":"t1", "from":"S0", "guard":"x<5", "action":"x=x+1", "to":"S1" }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transition {

    private String name;
    private String from;
    private String guard;
    private String action;
    private String to;

    public Transition() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getGuard() { return guard; }
    public void setGuard(String guard) { this.guard = guard; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public boolean hasGuard() { return guard != null && !guard.isBlank(); }
    public boolean hasAction() { return action != null && !action.isBlank(); }

    @Override
    public String toString() {
        return name + ": " + from + " -> " + to
                + (hasGuard() ? " [" + guard + "]" : " [true]")
                + (hasAction() ? " / " + action : "");
    }
}
