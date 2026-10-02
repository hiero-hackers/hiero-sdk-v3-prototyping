package org.hiero.sdk.v3.metalang.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.Requires;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Namespace-level rules: duplicate declarations and the validity of {@code requires} statements.
 * (Unused imports are reported by {@link TypeReferenceCheck}, which tracks usages.)
 */
final class NamespaceCheck implements Check {

    @Override
    public void run(final SpecModel model, final DiagnosticCollector out) {
        for (final String namespace : model.namespaceNames()) {
            final List<SchemaFile> files = model.filesOf(namespace);
            if (files.size() > 1) {
                for (final SchemaFile file : files.subList(1, files.size())) {
                    out.report(Rule.NAMESPACE_SPLIT, "Namespace '" + namespace + "' is also declared in "
                            + files.stream().filter(f -> f != file).map(SchemaFile::file)
                            .collect(Collectors.joining(", ")), file.location());
                }
            }
            final Map<String, Declaration> seen = new HashMap<>();
            for (final SchemaFile file : files) {
                for (final Declaration declaration : file.declarations()) {
                    if (declaration instanceof Declaration.Function) {
                        continue;
                    }
                    final Declaration previous = seen.putIfAbsent(declaration.name(), declaration);
                    if (previous != null) {
                        out.report(Rule.NAMESPACE_DUPLICATE_DECLARATION, "'" + declaration.name()
                                + "' is already declared at " + previous.location(), declaration.location());
                    }
                }
            }
        }
        for (final SchemaFile file : model.files()) {
            checkRequires(model, file, out);
        }
    }

    private static void checkRequires(final SpecModel model, final SchemaFile file, final DiagnosticCollector out) {
        final Map<String, Requires> byNamespace = new HashMap<>();
        for (final Requires requires : file.requires()) {
            final Requires previous = byNamespace.putIfAbsent(requires.namespace(), requires);
            if (previous != null) {
                out.report(Rule.REQUIRES_DUPLICATE_NAMESPACE, "Merge with the requires statement for '"
                        + requires.namespace() + "' at line " + previous.location().line(), requires.location());
            }
            if (requires.namespace().equals(file.namespace())) {
                out.report(Rule.REQUIRES_SELF, "Types of the own namespace need no import", requires.location());
                continue;
            }
            if (!model.hasNamespace(requires.namespace())) {
                out.report(Rule.REQUIRES_UNKNOWN_NAMESPACE, "Namespace '" + requires.namespace()
                        + "' does not exist", requires.location());
                continue;
            }
            for (final String type : requires.types()) {
                if (model.type(requires.namespace(), type).isEmpty()) {
                    final List<String> elsewhere = model.namespacesDeclaring(type);
                    out.report(Rule.REQUIRES_UNKNOWN_TYPE, "Namespace '" + requires.namespace()
                            + "' declares no type '" + type + "'"
                            + (elsewhere.isEmpty() ? "" : " (declared in " + String.join(", ", elsewhere) + ")"),
                            requires.location());
                }
            }
        }
    }
}
