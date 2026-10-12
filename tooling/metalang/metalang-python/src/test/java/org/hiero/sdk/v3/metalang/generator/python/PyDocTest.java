package org.hiero.sdk.v3.metalang.generator.python;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PyDocTest {

    @Test
    void aShortTextShouldBeOneLine() {
        assertThat(PyDoc.docstring("    ", "The shard.")).isEqualTo("    \"\"\"The shard.\"\"\"\n");
        // a closing quote would merge with the delimiter
        assertThat(PyDoc.docstring("", "Say \"hi\"")).isEqualTo("\"\"\"Say \"hi\"\n\"\"\"\n");
    }

    @Test
    void proseShouldBeWrappedAndStructureKept() {
        final String text = ("word ".repeat(40)).strip() + "\n\n- first\n- second\n\n```\n  code\n\nmore code\n```";
        final String doc = PyDoc.docstring("    ", text);
        assertThat(doc.lines()).allMatch(line -> line.length() <= 110);
        assertThat(doc).contains("\n    - first\n    - second\n").contains("\n      code\n\n    more code\n");
        assertThat(doc).startsWith("    \"\"\"word word").endsWith("\n    \"\"\"\n");
    }

    @Test
    void backslashesAndTripleQuotesShouldBeEscaped() {
        assertThat(PyDoc.docstring("", "a \\ b \"\"\" c")).isEqualTo("\"\"\"a \\\\ b \\\"\\\"\\\" c\"\"\"\n");
    }

    @Test
    void annotationsShouldAddNotes() {
        assertThat(PyDoc.of("", null, null)).isEmpty();
        assertThat(PyDoc.of("", "  ", null)).isEmpty();
    }
}
