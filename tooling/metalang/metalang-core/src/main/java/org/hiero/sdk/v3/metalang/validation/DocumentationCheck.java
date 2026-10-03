package org.hiero.sdk.v3.metalang.validation;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.EnumValue;
import org.hiero.sdk.v3.metalang.ast.Field;
import org.hiero.sdk.v3.metalang.ast.Method;
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
                deprecation(declaration.name(), declaration, declaration.documentation(), declaration.location(),
                        out);
                if (declaration instanceof Declaration.TypeDeclaration type) {
                    for (final Field field : type.fields()) {
                        check(field.name(), field.documentation(), field.location(), out);
                        deprecation(field.name(), field, field.documentation(), field.location(), out);
                    }
                    for (final Method method : type.methods()) {
                        check(method.name(), method.documentation(), method.location(), out);
                        deprecation(method.name(), method, method.documentation(), method.location(), out);
                    }
                    if (type instanceof Declaration.EnumType enumType) {
                        for (final EnumValue value : enumType.values()) {
                            check(value.name(), value.documentation(), value.location(), out);
                            deprecation(value.name(), value, value.documentation(), value.location(), out);
                        }
                    }
                }
            }
        }
    }

    /**
     * A deprecated element must explain the deprecation in its documentation: the generators turn the paragraph that
     * mentions it into the deprecation text of the API documentation (e.g. Javadoc {@code @deprecated}).
     */
    private static void deprecation(final String name, final Annotated element, final String documentation,
                                    final SourceLocation location, final DiagnosticCollector out) {
        if (element.hasAnnotation("deprecated")
                && !documentation.toLowerCase(java.util.Locale.ROOT).contains("deprecat")) {
            out.report(Rule.DOC_DEPRECATED_WITHOUT_REASON, "'" + name + "' is @@deprecated, but its documentation "
                    + "does not explain why (add a paragraph like 'Deprecated because ...; use ... instead.')",
                    location);
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
