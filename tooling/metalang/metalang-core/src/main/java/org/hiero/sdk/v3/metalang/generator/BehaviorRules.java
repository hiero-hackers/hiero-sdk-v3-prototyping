package org.hiero.sdk.v3.metalang.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;

/**
 * The behavior rules ({@code @@rule("node-choice")}) a test checks: the qualified ids and the tag that carries them in
 * the results of a test run.
 *
 * <p>A rule is defined in the {@code ## Description} of a spec by the marker {@code `rule:<id>`} (see "Behavior rules"
 * in {@code guidelines/api-guideline.md}). Its qualified id is {@code <namespace>/<id>}, like the id of a test. A test
 * names a rule of its own namespace by the id alone and a rule of another namespace by the qualified id.
 */
public final class BehaviorRules {

    /** The name of the annotation, without the {@code @@} prefix. */
    public static final String ANNOTATION = "rule";

    /** The prefix of the tag that carries a rule of a test. */
    public static final String TAG_PREFIX = "rule:";

    /** A reference: a rule id, optionally qualified by a namespace ({@code consensusnode.client/node-choice}). */
    private static final Pattern REFERENCE =
            Pattern.compile("(?:[a-z][A-Za-z0-9]*(?:\\.[a-z][A-Za-z0-9]*)*/)?[a-z0-9]+(?:-[a-z0-9]+)*");

    private BehaviorRules() {
    }

    /**
     * Checks whether the text is a valid reference to a rule.
     *
     * @param text the text
     * @return {@code true} for {@code <id>} or {@code <namespace>/<id>}
     */
    public static boolean isReference(final String text) {
        Objects.requireNonNull(text, "text must not be null");
        return REFERENCE.matcher(text).matches();
    }

    /**
     * The qualified id of a reference.
     *
     * @param namespace the namespace of the test that holds the reference
     * @param reference the reference, {@code <id>} or {@code <namespace>/<id>}
     * @return {@code <namespace>/<id>}
     */
    public static String qualify(final String namespace, final String reference) {
        Objects.requireNonNull(namespace, "namespace must not be null");
        Objects.requireNonNull(reference, "reference must not be null");
        return reference.contains("/") ? reference : namespace + "/" + reference;
    }

    /**
     * The qualified ids of the rules a test references, in declaration order and without duplicates. An argument that
     * is not a reference is skipped; the validator reports it ({@code test.invalid}).
     *
     * @param namespace   the namespace of the test
     * @param annotations the annotations of the test
     * @return the qualified ids, empty if there is no {@code @@rule}
     */
    public static List<String> of(final String namespace, final List<Annotation> annotations) {
        Objects.requireNonNull(annotations, "annotations must not be null");
        final List<String> ids = new ArrayList<>();
        for (final Annotation annotation : annotations) {
            if (!annotation.name().equals(ANNOTATION)) {
                continue;
            }
            for (final Literal argument : annotation.arguments()) {
                if (argument instanceof Literal.StringLiteral reference && isReference(reference.value())) {
                    final String id = qualify(namespace, reference.value());
                    if (!ids.contains(id)) {
                        ids.add(id);
                    }
                }
            }
        }
        return List.copyOf(ids);
    }

    /**
     * The tag of a rule: it marks every generated test that checks the rule, so that the results of a test run can
     * be grouped by rule.
     *
     * @param qualifiedId the qualified id of the rule
     * @return {@code rule:<namespace>/<id>}
     */
    public static String tag(final String qualifiedId) {
        Objects.requireNonNull(qualifiedId, "qualifiedId must not be null");
        return TAG_PREFIX + qualifiedId;
    }
}
