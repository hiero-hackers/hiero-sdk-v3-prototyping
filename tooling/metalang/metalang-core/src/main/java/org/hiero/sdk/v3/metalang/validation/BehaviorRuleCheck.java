package org.hiero.sdk.v3.metalang.validation;

import java.util.HashMap;
import java.util.Map;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.BehaviorRule;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.TestCase;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.generator.BehaviorRules;

/**
 * Checks the behavior rules of the specs (the {@code rule.*} rules, see "Behavior rules" in
 * {@code guidelines/api-guideline.md}): their ids are unique per namespace, across all spec files of the namespace, and
 * every {@code @@rule} of a test names a rule that a spec defines. The form of a rule id and of a reference is checked
 * where it is read (the Markdown extractor resp. the test resolver).
 */
final class BehaviorRuleCheck implements Check {

    @Override
    public void run(final ValidationContext context, final DiagnosticCollector out) {
        final Map<String, SourceLocation> rules = new HashMap<>();
        for (final SchemaFile file : context.model().files()) {
            for (final BehaviorRule rule : file.rules()) {
                final String id = file.namespace() + "/" + rule.id();
                final SourceLocation previous = rules.putIfAbsent(id, rule.location());
                if (previous != null) {
                    out.report(Rule.RULE_DUPLICATE_ID, "Another rule of namespace '" + file.namespace()
                            + "' has the id '" + rule.id() + "' (" + previous + ")", rule.location());
                }
            }
        }
        for (final SchemaFile file : context.model().files()) {
            for (final TestCase test : file.tests()) {
                for (final Annotation annotation : test.annotations()) {
                    if (!annotation.name().equals(BehaviorRules.ANNOTATION)) {
                        continue;
                    }
                    for (final Literal argument : annotation.arguments()) {
                        if (argument instanceof Literal.StringLiteral reference
                                && BehaviorRules.isReference(reference.value())
                                && !rules.containsKey(BehaviorRules.qualify(file.namespace(), reference.value()))) {
                            out.report(Rule.RULE_UNKNOWN, "No spec defines the rule '"
                                    + BehaviorRules.qualify(file.namespace(), reference.value()) + "'; a rule is "
                                    + "defined by the marker `rule:<id>` in the '## Description' of a spec of its "
                                    + "namespace", annotation.location());
                        }
                    }
                }
            }
        }
    }
}
