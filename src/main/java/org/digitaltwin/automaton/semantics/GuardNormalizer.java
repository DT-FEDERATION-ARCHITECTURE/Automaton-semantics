package org.digitaltwin.automaton.semantics;

/**
 * Normalizes guard strings for JEXL evaluation.
 * - Single '=' becomes '==' (preserving >=, <=, !=, ==)
 * - Empty/null guard becomes "true" (always enabled)
 */
public class GuardNormalizer {

    public static String normalize(String rawGuard) {
        if (rawGuard == null || rawGuard.isBlank()) return "true";

        String g = rawGuard.trim();

        // Protect multi-char operators containing '='
        g = g.replace(">=", "\0GTE\0");
        g = g.replace("<=", "\0LTE\0");
        g = g.replace("!=", "\0NEQ\0");
        g = g.replace("==", "\0EQ\0");

        // Remaining single '=' → '=='
        g = g.replace("=", "==");

        // Restore
        g = g.replace("\0GTE\0", ">=");
        g = g.replace("\0LTE\0", "<=");
        g = g.replace("\0NEQ\0", "!=");
        g = g.replace("\0EQ\0", "==");

        return g;
    }
}
