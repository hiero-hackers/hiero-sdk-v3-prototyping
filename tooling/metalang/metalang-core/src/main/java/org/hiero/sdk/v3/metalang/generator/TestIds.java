package org.hiero.sdk.v3.metalang.generator;

import java.util.Locale;
import org.hiero.sdk.v3.metalang.model.QualifiedName;

/**
 * The language-neutral ids of generated tests (ADR-0012): every generator writes the same id for the same test, so the
 * results of the languages can be compared. The id travels in the results of a test run (Open Test Reporting) as a
 * tag, {@code testId:<id>}.
 *
 * <ul>
 *   <li>A test of a spec ({@code tests} block): {@code <namespace>/<@@testId>}, e.g.
 *       {@code keys/a-hex-key-may-have-a-0x-prefix} (see {@code TestDefinition.id()}).</li>
 *   <li>A contract test of a type: {@code <namespace>.<Type>/<element>/<check>}, e.g.
 *       {@code ledger.AccountId/num/accepts-at-minimum} or {@code keys.PrivateKey/sign/call}; of a namespace-level
 *       function: {@code <namespace>/<function>/<check>}. A check of the whole type has no element
 *       ({@code ledger.AccountId/create}). Elements keep the names of the specs; checks are kebab-case words.</li>
 * </ul>
 *
 * <p>The ids contain no whitespace and none of {@code , ( ) & | !}, so that they are valid JUnit tags.
 */
public final class TestIds {

    /** The prefix of the tag that carries the id of a test. */
    public static final String TAG_PREFIX = "testId:";

    /** Creates an object with the values of the constructor and returns them. */
    public static final String CREATE = "create";
    /** Creates an object without the optional values: they are {@code null} or their default. */
    public static final String CREATE_WITH_DEFAULTS = "create-with-defaults";
    /** Equal values give equal objects and hash codes. */
    public static final String VALUE_SEMANTICS = "value-semantics";
    /** The constants of an enum have the attribute values of the specs. */
    public static final String CONSTANTS = "constants";
    /** The builder builds the values of the constructor. */
    public static final String BUILDER_BUILDS = "builder/builds";
    /** A value the builder does not set is {@code null} or its default. */
    public static final String BUILDER_OPTIONAL = "builder/optional-values";
    /** The builder rejects a missing required value. */
    public static final String BUILDER_MISSING = "builder/rejects-missing-value";
    /** A method or function can be called. */
    public static final String CALL = "call";
    /** {@code null} is rejected. */
    public static final String REJECTS_NULL = "rejects-null";
    /** The object or the setter copies a value in (and the accessor out). */
    public static final String COPIES = "copies";
    /** The setter changes the value. */
    public static final String SET = "set";
    /** The setter accepts {@code null}. */
    public static final String SET_NULL = "set-null";
    /** The setter rejects {@code null} and keeps the value. */
    public static final String SET_REJECTS_NULL = "set-rejects-null";

    private TestIds() {
    }

    /**
     * The tag of a test id.
     *
     * @param id the id
     * @return {@code testId:<id>}
     */
    public static String tag(final String id) {
        return TAG_PREFIX + id;
    }

    /**
     * The scope of the contract tests of a type.
     *
     * @param type the type
     * @return {@code <namespace>.<Type>}
     */
    public static String scope(final QualifiedName type) {
        return type.namespace() + "." + type.name();
    }

    /**
     * The id of a contract test.
     *
     * @param scope the scope: {@link #scope(QualifiedName)} or the namespace of a function
     * @param parts the element (if any) and the check
     * @return {@code <scope>/<part>/...}
     */
    public static String contract(final String scope, final String... parts) {
        return scope + "/" + String.join("/", parts);
    }

    /**
     * The check that a value is accepted.
     *
     * @param valueCase the boundary case, e.g. {@code AtMaximumBytes} or {@code a value with the maximum length}
     * @return e.g. {@code accepts-at-maximum-bytes}
     */
    public static String accepts(final String valueCase) {
        return "accepts-" + kebab(valueCase);
    }

    /**
     * The check that a value is rejected.
     *
     * @param valueCase the boundary case, e.g. {@code BelowMinimum}
     * @return e.g. {@code rejects-below-minimum}
     */
    public static String rejects(final String valueCase) {
        return "rejects-" + kebab(valueCase);
    }

    /**
     * Text or a camel-case name in kebab-case: {@code AtMaximumBytes} and {@code at maximum bytes} both become
     * {@code at-maximum-bytes}.
     *
     * @param text the text
     * @return lowercase words of letters and digits, joined by hyphens
     */
    public static String kebab(final String text) {
        return text.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
