package org.hiero.sdk.v3.metalang.check;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ApiDifferenceTest {

    @Test
    void shouldRenderAndSortByFileLineAndMessage() {
        final ApiDifference noFile = new ApiDifference("", 0, "Type a.B is missing");
        final ApiDifference noLine = new ApiDifference("a/B.java", 0, "x");
        final ApiDifference line2 = new ApiDifference("a/B.java", 2, "y");
        final ApiDifference line2b = new ApiDifference("a/B.java", 2, "z");
        assertThat(noFile).hasToString("Type a.B is missing");
        assertThat(noLine).hasToString("a/B.java: x");
        assertThat(line2).hasToString("a/B.java:2: y");
        assertThat(Stream.of(line2b, line2, noLine, noFile).sorted().toList())
                .isEqualTo(List.of(noFile, noLine, line2, line2b));
    }
}
