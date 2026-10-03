package org.hiero.sdk.v3.metalang.validation;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Checks the text that becomes public API documentation — the {@code ## Description} of a spec and the comments
 * attached to declarations — for references to spec internals (guideline section "Write documentation for the
 * users of the API"). The check is a heuristic for typical leaks; it cannot judge prose.
 */
final class DocumentationCheck implements Check {

    private record Leak(Pattern pattern, String what) {
    }

    private static final List<Leak> LEAKS = List.of(
            new Leak(Pattern.compile("[\\w./-]+\\.md\\b"), "a reference to a spec file"),
            new Leak(Pattern.compile("@@[a-zA-Z]+"), "a meta-language annotation"),
            new Leak(Pattern.compile("\\bADR-\\d+"), "a reference to an ADR"),
            new Leak(Pattern.compile("\\bTODO\\b"), "a TODO"));

    @Override
    public void run(final ValidationContext context, final DiagnosticCollector out) {
        final SpecModel model = context.model();
        for (final SchemaFile file : model.files()) {
            final List<String> lines = file.description().lines().toList();
            for (int i = 0; i < lines.size(); i++) {
                final int line = file.descriptionLine() + i;
                leak(lines.get(i)).ifPresent(message -> out.report(Rule.DOC_INTERNAL_REFERENCE,
                        "Description contains " + message, new SourceLocation(file.file(), line, 1)));
            }
            for (final Declaration declaration : file.declarations()) {
                check(declaration.name(), declaration.documentation(), declaration.location(), out);
                if (declaration instanceof Declaration.TypeDeclaration type) {
                    type.fields().forEach(f -> check(f.name(), f.documentation(), f.location(), out));
                    type.methods().forEach(m -> check(m.name(), m.documentation(), m.location(), out));
                    if (type instanceof Declaration.EnumType enumType) {
                        enumType.values().forEach(v -> check(v.name(), v.documentation(), v.location(), out));
                    }
                }
            }
        }
    }

    private static void check(final String name, final String documentation, final SourceLocation location,
                              final DiagnosticCollector out) {
        leak(documentation).ifPresent(message -> out.report(Rule.DOC_INTERNAL_REFERENCE,
                "Documentation of '" + name + "' contains " + message
                        + "; write it for API users or separate the remark by a blank line", location));
    }

    private static Optional<String> leak(final String text) {
        for (final Leak leak : LEAKS) {
            final Matcher matcher = leak.pattern().matcher(text);
            if (matcher.find()) {
                return Optional.of(leak.what() + " ('" + matcher.group() + "')");
            }
        }
        return Optional.empty();
    }
}
