package org.hiero.sdk.v3.metalang.cli;

import java.util.List;
import java.util.Locale;
import org.hiero.sdk.v3.metalang.ValidationReport;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.diagnostic.Severity;

/**
 * Renders a report as JSON. Hand-written to keep the tool dependency-free; the output is stable
 * (fixed key order, sorted diagnostics, LF line endings).
 */
final class JsonReport {

    private JsonReport() {
    }

    static String render(final ValidationReport report, final List<Diagnostic> diagnostics) {
        final StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"specs\": ").append(report.specCount()).append(",\n");
        json.append("  \"errors\": ").append(report.count(Severity.ERROR)).append(",\n");
        json.append("  \"warnings\": ").append(report.count(Severity.WARNING)).append(",\n");
        json.append("  \"infos\": ").append(report.count(Severity.INFO)).append(",\n");
        json.append("  \"diagnostics\": [");
        for (int i = 0; i < diagnostics.size(); i++) {
            final Diagnostic d = diagnostics.get(i);
            json.append(i == 0 ? "\n" : ",\n");
            json.append("    {\"file\": ").append(quote(d.location().file()))
                    .append(", \"line\": ").append(d.location().line())
                    .append(", \"column\": ").append(d.location().column())
                    .append(", \"severity\": ").append(quote(d.severity().name().toLowerCase(Locale.ROOT)))
                    .append(", \"rule\": ").append(quote(d.ruleId()))
                    .append(", \"message\": ").append(quote(d.message()))
                    .append('}');
        }
        json.append(diagnostics.isEmpty() ? "]\n" : "\n  ]\n");
        json.append("}\n");
        return json.toString();
    }

    static String quote(final String value) {
        final StringBuilder result = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            switch (c) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (c < 0x20) {
                        result.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
                    } else {
                        result.append(c);
                    }
                }
            }
        }
        return result.append('"').toString();
    }
}
