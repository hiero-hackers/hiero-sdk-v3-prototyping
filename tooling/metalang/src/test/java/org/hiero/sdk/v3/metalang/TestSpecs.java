package org.hiero.sdk.v3.metalang;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;

/**
 * Test helper that wraps plain schemas into well-formed spec Markdown and validates them.
 */
public final class TestSpecs {

    private TestSpecs() {
    }

    /**
     * Wraps a schema into a Markdown document with the full canonical skeleton.
     */
    public static String markdown(final String schema) {
        return """
                # Title

                ## Description

                Text.

                ## API Schema

                ```
                %s```

                ## Testing

                Nothing to test.

                ## Questions & Comments
                """.formatted(schema.endsWith("\n") ? schema : schema + "\n");
    }

    /**
     * Validates the given schemas (one spec file per schema, named {@code spec0.md}, {@code spec1.md}, ...).
     */
    public static ValidationReport validate(final String... schemas) {
        final Map<String, String> documents = new LinkedHashMap<>();
        for (int i = 0; i < schemas.length; i++) {
            documents.put("spec" + i + ".md", markdown(schemas[i]));
        }
        return new MetaLang().validate(documents);
    }

    /**
     * Returns the diagnostics for the given schemas, excluding document-structure findings.
     */
    public static List<Diagnostic> diagnostics(final String... schemas) {
        return validate(schemas).diagnostics().stream()
                .filter(d -> !d.ruleId().startsWith("doc."))
                .toList();
    }

    /**
     * Returns the rule ids of all non-document findings.
     */
    public static List<String> ruleIds(final String... schemas) {
        return diagnostics(schemas).stream().map(Diagnostic::ruleId).toList();
    }
}
