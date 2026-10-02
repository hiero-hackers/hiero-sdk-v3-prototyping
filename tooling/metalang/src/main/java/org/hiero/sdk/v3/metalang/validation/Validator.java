package org.hiero.sdk.v3.metalang.validation;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Validates a {@link SpecModel} against the rules of the meta-language guideline.
 *
 * <p>The validator is stateless and deterministic: the same model always produces the same, sorted
 * list of diagnostics.
 */
public final class Validator {

    private final List<Check> checks = List.of(
            new SyntaxVariantCheck(),
            new NamingCheck(),
            new NamespaceCheck(),
            new TypeReferenceCheck(),
            new AnnotationCheck(),
            new OneOfCheck(),
            new InheritanceCheck(),
            new MemberCheck(),
            new GenericMethodCheck(),
            new EnumCheck());

    /**
     * Validates the model.
     *
     * @param model the model
     * @return all findings in deterministic order
     */
    public List<Diagnostic> validate(final SpecModel model) {
        Objects.requireNonNull(model, "model must not be null");
        final DiagnosticCollector out = new DiagnosticCollector();
        for (final Check check : checks) {
            check.run(model, out);
        }
        return out.sorted();
    }
}
