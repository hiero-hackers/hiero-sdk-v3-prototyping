package org.hiero.sdk.v3.metalang.generator.go;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

class GoDocTest {

    /** A declaration carrying exactly the annotations a case needs. */
    private record Declaration(List<Annotation> annotations) implements Annotated {
        private static final SourceLocation SOMEWHERE = new SourceLocation("f", 1, 1);

        static Declaration of(final String... names) {
            return new Declaration(java.util.Arrays.stream(names)
                    .map(name -> new Annotation(name, List.of(), false, SOMEWHERE)).toList());
        }

        @Override
        public SourceLocation location() {
            return SOMEWHERE;
        }
    }

    @org.junit.jupiter.api.Test
    void shouldStartTheCommentWithTheIdentifierItDocuments() {
        // the convention `go doc` and the editors expect
        assertThat(GoDoc.of("AccountID", "Identifies an account.", Declaration.of()))
                .isEqualTo("// AccountID identifies an account.\n");
    }

    @org.junit.jupiter.api.Test
    void shouldNotRepeatTheNameWhenTheTextAlreadyStartsWithIt() {
        assertThat(GoDoc.of("AccountID", "AccountID identifies an account.", Declaration.of()))
                .isEqualTo("// AccountID identifies an account.\n");
    }

    @org.junit.jupiter.api.Test
    void shouldKeepTheCaseOfAWordThatIsItselfAName() {
        // lowering "HAPI" would be wrong; a second capital letter is what distinguishes a name from a sentence
        assertThat(GoDoc.of("Authority", "HAPI key of an account.", Declaration.of()))
                .isEqualTo("// Authority HAPI key of an account.\n");
    }

    @org.junit.jupiter.api.Test
    void shouldSayNothingWithoutDocumentation() {
        assertThat(GoDoc.of("X", "", Declaration.of())).isEmpty();
        assertThat(GoDoc.of("X", null, Declaration.of())).isEmpty();
    }

    @org.junit.jupiter.api.Test
    void shouldAddTheDeprecationParagraphGoVetRecognises() {
        assertThat(GoDoc.of("X", "Does a thing.", Declaration.of("deprecated"))).isEqualTo("""
                // X does a thing.
                //
                // Deprecated: this declaration should no longer be used.
                """);
        // without documentation the paragraph stands on its own
        assertThat(GoDoc.of("X", "", Declaration.of("deprecated")))
                .isEqualTo("// Deprecated: this declaration should no longer be used.\n");
    }

    @org.junit.jupiter.api.Test
    void shouldWrapLongTextAndSeparateParagraphs() {
        final String text = "Word ".repeat(40).trim() + "\n\nA second paragraph.";

        final String comment = GoDoc.comment(text);

        assertThat(comment.lines()).allMatch(line -> line.startsWith("//") && line.length() <= 112);
        assertThat(comment).contains("//\n// A second paragraph.\n");
    }

    @org.junit.jupiter.api.Test
    void shouldKeepAnIndentedParagraphVerbatim() {
        // an indented block is how a Go doc comment writes code, and rewrapping it would destroy it
        final String comment = GoDoc.comment("Use it like this:\n\n\tx := NewAccountID(0, 0)\n\tuse(x)");

        assertThat(comment).isEqualTo("""
                // Use it like this:
                //
                //\tx := NewAccountID(0, 0)
                //\tuse(x)
                """);
    }
}
