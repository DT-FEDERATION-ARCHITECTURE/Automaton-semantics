package org.digitaltwin.automaton.semantics;

import org.apache.commons.jexl3.*;
import java.util.Map;

/**
 * Executes action expressions to produce an updated valuation.
 */
public class ActionEvaluator {

    private static final JexlEngine JEXL = new JexlBuilder()
            .strict(false).silent(true).create();

    public static Valuation execute(String rawAction, Valuation valuation) {
        Valuation result = valuation.copy();
        if (rawAction == null || rawAction.isBlank()) return result;

        for (String stmt : rawAction.split(";")) {
            stmt = stmt.trim();
            if (stmt.isEmpty()) continue;

            int eqIdx = findAssignmentEquals(stmt);
            if (eqIdx < 0)
                throw new RuntimeException("Action must be assignment: '" + stmt + "'");

            String varName = stmt.substring(0, eqIdx).trim();
            String exprStr = stmt.substring(eqIdx + 1).trim();

            try {
                JexlExpression expr = JEXL.createExpression(exprStr);
                JexlContext ctx = new MapContext();
                for (Map.Entry<String, Object> e : result.entries()) {
                    ctx.set(e.getKey(), e.getValue());
                }
                result.set(varName, expr.evaluate(ctx));
            } catch (JexlException e) {
                throw new RuntimeException("Action failed: '" + stmt + "' -> " + e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * Find the assignment '=' that is NOT part of ==, >=, <=, !=
     */
    private static int findAssignmentEquals(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '=') continue;
            // Skip if preceded by >, <, !, =
            if (i > 0 && "><!=".indexOf(s.charAt(i - 1)) >= 0) continue;
            // Skip if followed by =
            if (i + 1 < s.length() && s.charAt(i + 1) == '=') continue;
            return i;
        }
        return -1;
    }
}
