package org.hiero.sdk.v3.metalang.validation;

import java.util.regex.Pattern;
import org.hiero.sdk.v3.metalang.ast.Comment;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.Method;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.semantic.ResolvedType;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Reports the non-standard syntax variants the lenient grammar accepts.
 */
final class SyntaxVariantCheck implements Check {

    /** A comment that starts with an annotation, e.g. {@code // @@throws(x) if ...}. */
    private static final Pattern ANNOTATION_COMMENT = Pattern.compile("^@@[a-zA-Z]+(\\(.*|\\s*)$");

    @Override
    public void run(final SpecModel model, final DiagnosticCollector out) {
        for (final SchemaFile file : model.files()) {
            for (final Comment comment : file.comments()) {
                if (ANNOTATION_COMMENT.matcher(comment.text()).matches()) {
                    out.report(Rule.SYNTAX_ANNOTATION_IN_COMMENT, "Annotation in comment is ignored: '"
                            + comment.text() + "'", comment.location());
                }
            }
            for (final Declaration declaration : file.declarations()) {
                switch (declaration) {
                    case Declaration.ComplexType type -> {
                        if (type.typeKeyword()) {
                            out.report(Rule.SYNTAX_TYPE_KEYWORD, "Remove the 'type' keyword before '"
                                    + type.name() + "'", type.location());
                        }
                        type.methods().forEach(m -> checkMethodSyntax(m, out));
                    }
                    case Declaration.EnumType enumType -> {
                        for (final SourceLocation placeholder : enumType.placeholders()) {
                            out.report(Rule.SYNTAX_ENUM_PLACEHOLDER, "Enum '" + enumType.name()
                                    + "' uses '...' instead of listing all values", placeholder);
                        }
                        enumType.methods().forEach(m -> checkMethodSyntax(m, out));
                    }
                    case Declaration.Function function -> checkFunction(model, file, function, out);
                    case Declaration.Constant ignored -> {
                        // nothing to check
                    }
                }
            }
        }
    }

    private static void checkMethodSyntax(final Method method, final DiagnosticCollector out) {
        switch (method.syntax()) {
            case TRAILING_RETURN -> out.report(Rule.SYNTAX_TRAILING_RETURN_TYPE, "Write '"
                    + method.returnType().text() + " " + method.name() + "(...)'", method.location());
            case MISSING_RETURN -> out.report(Rule.SYNTAX_MISSING_RETURN_TYPE, "Method '" + method.name()
                    + "' has no return type; write 'void " + method.name() + "(...)'", method.location());
            case CLASSIC -> {
                // standard form
            }
        }
    }

    private static void checkFunction(final SpecModel model, final SchemaFile file,
                                      final Declaration.Function function, final DiagnosticCollector out) {
        if (function.owner() == null) {
            out.report(Rule.SYNTAX_NAMESPACE_FUNCTION, "Function '" + function.name()
                    + "' is declared at namespace level", function.location());
            return;
        }
        out.report(Rule.SYNTAX_DETACHED_MEMBER, "Declare '" + function.method().name() + "' inside type '"
                + function.owner() + "'", function.location());
        if (!(model.resolve(file, function.owner()) instanceof ResolvedType.Declared declared)
                || !declared.namespace().equals(file.namespace())) {
            out.report(Rule.TYPE_UNKNOWN, "Owner type '" + function.owner() + "' is not declared in namespace '"
                    + file.namespace() + "'", function.location());
        }
    }
}
