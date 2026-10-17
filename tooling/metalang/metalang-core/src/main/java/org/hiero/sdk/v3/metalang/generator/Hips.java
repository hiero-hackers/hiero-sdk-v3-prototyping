package org.hiero.sdk.v3.metalang.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;

/**
 * The Hiero Improvement Proposals ({@code @@hip("869")}) an element comes from: the numbers, their documentation
 * links and the tag that carries them in the results of a test run.
 *
 * <p>A HIP is identified by its number alone; the link points at the proposal's Markdown file in the
 * <a href="https://github.com/hiero-ledger/hiero-improvement-proposals">hiero-improvement-proposals</a> repository,
 * the same for every language, so that the generated documentation of all SDKs references the same source.
 */
public final class Hips {

    /** The name of the annotation, without the {@code @@} prefix. */
    public static final String ANNOTATION = "hip";

    /** The prefix of the tag that carries the HIP of a test. */
    public static final String TAG_PREFIX = "hip:";

    /** The directory of the HIP files in the repository; a number is appended to it, followed by {@code .md}. */
    private static final String BASE_URL =
            "https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-";

    /** A HIP number: digits without a leading zero, as written in the file name. */
    private static final Pattern NUMBER = Pattern.compile("[1-9][0-9]*");

    private Hips() {
    }

    /**
     * Checks whether the text is a HIP number.
     *
     * @param text the text
     * @return {@code true} if it is a number without a leading zero
     */
    public static boolean isNumber(final String text) {
        Objects.requireNonNull(text, "text must not be null");
        return NUMBER.matcher(text).matches();
    }

    /**
     * The HIP numbers of an element, in declaration order and without duplicates. An argument that is not a HIP
     * number is skipped; the validator reports it ({@code annotation.arguments}).
     *
     * @param element the annotated element (a declaration of the model or a node of the syntax tree)
     * @return the numbers, empty if the element has no {@code @@hip}
     */
    public static List<String> of(final Annotated element) {
        Objects.requireNonNull(element, "element must not be null");
        return of(element.annotations());
    }

    /**
     * The HIP numbers of a list of annotations, in declaration order and without duplicates. An argument that is not
     * a HIP number is skipped; the validator reports it ({@code annotation.arguments}).
     *
     * @param annotations the annotations of an element
     * @return the numbers, empty if there is no {@code @@hip}
     */
    public static List<String> of(final List<Annotation> annotations) {
        Objects.requireNonNull(annotations, "annotations must not be null");
        final List<String> numbers = new ArrayList<>();
        for (final Annotation annotation : annotations) {
            if (!annotation.name().equals(ANNOTATION)) {
                continue;
            }
            for (final Literal argument : annotation.arguments()) {
                if (argument instanceof Literal.StringLiteral number && isNumber(number.value())
                        && !numbers.contains(number.value())) {
                    numbers.add(number.value());
                }
            }
        }
        return List.copyOf(numbers);
    }

    /**
     * The link to the Markdown file of a HIP.
     *
     * @param number the HIP number
     * @return the URL of the file in the repository
     */
    public static String url(final String number) {
        Objects.requireNonNull(number, "number must not be null");
        return BASE_URL + number + ".md";
    }

    /**
     * The name of a HIP as it is written in prose.
     *
     * @param number the HIP number
     * @return {@code HIP-<number>}
     */
    public static String label(final String number) {
        Objects.requireNonNull(number, "number must not be null");
        return "HIP-" + number;
    }

    /**
     * The tag of a HIP: it marks every generated test of an element that comes from the proposal, so that a test run
     * can be filtered by it.
     *
     * @param number the HIP number
     * @return {@code hip:<number>}
     */
    public static String tag(final String number) {
        Objects.requireNonNull(number, "number must not be null");
        return TAG_PREFIX + number;
    }
}
