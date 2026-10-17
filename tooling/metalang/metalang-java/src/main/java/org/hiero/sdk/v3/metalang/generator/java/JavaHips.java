package org.hiero.sdk.v3.metalang.generator.java;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.generator.Hips;

/**
 * The Java form of {@code @@hip("869")}: a {@code @see} tag that links the proposal in the documentation of the
 * element, and the JUnit tag of its generated tests.
 *
 * <p>The link is written as HTML ({@code @see <a href="...">HIP-869</a>}) because the {@code @see} tag of a Markdown
 * documentation comment (JEP 467) does not accept a Markdown link; inside the text of another tag the Markdown form
 * is used.
 */
final class JavaHips {

    private JavaHips() {
    }

    /**
     * The {@code @see} tags of an element, to be appended to the block tags of its documentation comment.
     *
     * @param element the annotated element
     * @return one tag per HIP, empty if the element has no {@code @@hip}
     */
    static List<String> seeTags(final Annotated element) {
        return seeTags(element.annotations());
    }

    /**
     * The {@code @see} tags of a list of annotations.
     *
     * @param annotations the annotations of an element
     * @return one tag per HIP, empty if there is no {@code @@hip}
     */
    static List<String> seeTags(final List<Annotation> annotations) {
        return Hips.of(annotations).stream()
                .map(number -> "@see <a href=\"" + Hips.url(number) + "\">" + Hips.label(number) + "</a>")
                .toList();
    }

    /**
     * The block tags of an element with its {@code @see} tags appended.
     *
     * @param tags    the tags so far
     * @param element the annotated element
     * @return the tags, unchanged if the element has no {@code @@hip}
     */
    static List<String> withSeeTags(final List<String> tags, final Annotated element) {
        final List<String> hips = seeTags(element);
        if (hips.isEmpty()) {
            return tags;
        }
        final List<String> all = new ArrayList<>(tags);
        all.addAll(hips);
        return all;
    }

    /**
     * The HIPs of an element as Markdown links in parentheses, to be appended to the text of another tag (e.g. the
     * {@code @param} tag of a record component).
     *
     * @param annotations the annotations of an element
     * @return {@code  ([HIP-869](...))}, with a leading blank; empty if there is no {@code @@hip}
     */
    static String inline(final List<Annotation> annotations) {
        final List<String> numbers = Hips.of(annotations);
        if (numbers.isEmpty()) {
            return "";
        }
        return " (" + numbers.stream().map(n -> "[" + Hips.label(n) + "](" + Hips.url(n) + ")")
                .collect(Collectors.joining(", ")) + ")";
    }
}
