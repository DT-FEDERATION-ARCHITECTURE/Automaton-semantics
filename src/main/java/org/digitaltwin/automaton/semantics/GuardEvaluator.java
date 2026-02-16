package org.digitaltwin.automaton.semantics;

import org.apache.commons.jexl3.*;
import java.util.Map;

/**
 * Evaluates a guard expression against a Valuation.
 * Returns true if the guard is satisfied, false otherwise.
 */
public class GuardEvaluator {

    private static final JexlEngine JEXL = new JexlBuilder()
            .strict(false).silent(true).create();

    public static boolean evaluate(String rawGuard, Valuation valuation) {
        String normalized = GuardNormalizer.normalize(rawGuard);

        if ("true".equals(normalized)) return true;

        try {
            JexlExpression expr = JEXL.createExpression(normalized);
            JexlContext ctx = new MapContext();
            for (Map.Entry<String, Object> e : valuation.entries()) {
                ctx.set(e.getKey(), e.getValue());
            }
            Object result = expr.evaluate(ctx);
            if (result instanceof Boolean b) return b;
            if (result == null) return false;
            throw new RuntimeException("Guard '" + rawGuard + "' returned non-boolean: " + result);
        } catch (JexlException e) {
            throw new RuntimeException("Guard evaluation failed: '" + rawGuard + "' -> " + e.getMessage(), e);
        }
    }
}
