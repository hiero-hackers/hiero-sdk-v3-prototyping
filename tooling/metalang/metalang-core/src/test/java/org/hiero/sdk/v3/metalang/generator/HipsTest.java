package org.hiero.sdk.v3.metalang.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.junit.jupiter.api.Test;

class HipsTest {

    private static final SourceLocation LOCATION = new SourceLocation("a.md", 1, 1);

    private static Annotation annotation(final String name, final String... arguments) {
        return new Annotation(name, List.of(arguments).stream()
                .map(a -> (Literal) new Literal.StringLiteral(a, LOCATION)).toList(), true, LOCATION);
    }

    @Test
    void shouldLinkTheMarkdownFileOfTheProposal() {
        assertThat(Hips.url("869"))
                .isEqualTo("https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-869.md");
        assertThat(Hips.label("1046")).isEqualTo("HIP-1046");
        assertThat(Hips.tag("1046")).isEqualTo("hip:1046");
    }

    @Test
    void shouldAcceptNumbersWithoutALeadingZero() {
        assertThat(Hips.isNumber("869")).isTrue();
        assertThat(Hips.isNumber("1")).isTrue();
        assertThat(Hips.isNumber("0869")).isFalse();
        assertThat(Hips.isNumber("")).isFalse();
        assertThat(Hips.isNumber("HIP-869")).isFalse();
        assertThat(Hips.isNumber("86 9")).isFalse();
    }

    @Test
    void shouldReadTheNumbersOfTheAnnotationInOrderAndWithoutDuplicates() {
        assertThat(Hips.of(List.of(annotation("immutable"), annotation("hip", "869", "1046", "869"))))
                .containsExactly("869", "1046");
        assertThat(Hips.of(List.of(annotation("deprecated", "old")))).isEmpty();
    }

    @Test
    void shouldSkipAnArgumentThatIsNoNumber() {
        // the validator reports it (annotation.arguments); the generators must not write a broken link
        assertThat(Hips.of(List.of(annotation("hip", "HIP-869", "1046")))).containsExactly("1046");
    }
}
