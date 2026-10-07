package org.hiero.sdk.v3.metalang.generator.go;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class GoFormatTest {

    @Test
    void shouldPadEveryFirstCellToTheWidestOne() {
        assertThat(GoFormat.block(List.of("\tid int32", "\tdisplayName string", "\tok bool")))
                .isEqualTo("\tid          int32\n\tdisplayName string\n\tok          bool\n");
    }

    @Test
    void shouldStartANewColumnAfterALineThatIsNotADeclaration() {
        // gofmt does not align a constant that carries its own comment with the constants above it
        assertThat(GoFormat.block(List.of("\tLow Level = iota", "\t// Deprecated: gone", "\tHigher Level")))
                .isEqualTo("\tLow Level = iota\n\t// Deprecated: gone\n\tHigher Level\n");
    }

    @Test
    void shouldLeaveALineWithOneCellAlone() {
        assertThat(GoFormat.block(List.of("\tLevelLow Level = iota", "\tLevelHigh")))
                .isEqualTo("\tLevelLow Level = iota\n\tLevelHigh\n");
    }

    @Test
    void shouldHandleTheEmptyBlock() {
        assertThat(GoFormat.block(List.<String>of())).isEmpty();
        assertThat(GoFormat.block("")).isEmpty();
    }

    @Test
    void shouldAcceptTextAsWellAsLines() {
        assertThat(GoFormat.block("\ta int32\n\tbb string\n")).isEqualTo("\ta  int32\n\tbb string\n");
    }
}
