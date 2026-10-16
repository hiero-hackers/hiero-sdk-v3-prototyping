package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The checks of {@code @@oneOf} and {@code @@oneOrNoneOf} in the generated constructors: groups of immutable
 * attributes are checked by records and classes, groups of mutable attributes are not; the generated tests build
 * valid values and check the rejections.
 */
class JavaOneOfTest {

    private static final String SCHEMA = """
            namespace a

            @@oneOf(email, phone)
            Contact {
                @@immutable @@nullable email: string
                @@immutable @@nullable phone: string
                @@immutable @@nullable note: string
            }

            @@oneOrNoneOf(first, second)
            Pick {
                @@immutable @@nullable first: int64
                @@immutable @@nullable second: bytes
            }

            abstraction Shape {}

            @@oneOf(code, name)
            Mark extends Shape {
                @@immutable @@nullable code: int32
                @@immutable @@nullable name: string
                size: int32
            }

            @@oneOf(fileId, contractId)
            Pending {
                @@nullable fileId: string
                @@nullable contractId: string
            }

            Holder {
                @@immutable contact: Contact
            }
            """;

    @TempDir
    Path temp;

    private static List<GeneratedFile> generate() {
        return new JavaGenerator(JavaGeneratorConfig.DEFAULT).generate(LinkedModel.of(new MetaLang().validate(
                Map.of("f/a.md", TestSpecs.markdown(SCHEMA))).model()));
    }

    private static String source(final List<GeneratedFile> files, final String type) {
        return files.stream().filter(f -> f.path().endsWith("/" + type + ".java")).findFirst().orElseThrow()
                .content();
    }

    @Test
    void theConstructorsShouldCheckTheGroupsOfImmutableAttributes() {
        final List<GeneratedFile> files = generate();
        assertThat(source(files, "Contact")).contains("""
                        if ((email != null ? 1 : 0) + (phone != null ? 1 : 0) != 1) {
                            throw new IllegalArgumentException("exactly one of email, phone must be set");
                        }
                """);
        assertThat(source(files, "Pick")).contains("""
                        if ((first != null ? 1 : 0) + (second != null ? 1 : 0) > 1) {
                            throw new IllegalArgumentException("at most one of first, second may be set");
                        }
                """);
        assertThat(source(files, "Mark")).contains("if ((code != null ? 1 : 0) + (name != null ? 1 : 0) != 1) {");
        // mutable attributes are set one by one: the group can only hold when the object is used
        assertThat(source(files, "Pending")).doesNotContain("IllegalArgumentException");
    }

    @Test
    void theGeneratedValuesAndTestsShouldRespectTheGroups() throws Exception {
        final List<GeneratedFile> files = generate();
        final GeneratedJava.Compilation modules = GeneratedJava.compile(files, temp);
        assertThat(modules.diagnostics()).isEmpty();
        final GeneratedJava.Compilation tests = GeneratedJava.compileTests(files, modules, temp);
        assertThat(tests.diagnostics()).isEmpty();
        final GeneratedJava.TestRun run = GeneratedJava.runTests(files, tests);
        assertThat(run.unexpected()).isEmpty();
        assertThat(source(files, "ContactTest"))
                .contains("void shouldRejectTwoOfEmailOrPhone()")
                .contains("void shouldRejectNoneOfEmailOrPhone()")
                // create(): the first attribute of the group is set, the second is null
                .contains("return new Contact(emailValue(), (String) null, noteValue());");
        assertThat(source(files, "PickTest")).contains("void shouldRejectTwoOfFirstOrSecond()")
                .doesNotContain("shouldRejectNoneOf");
        // a value of a type with a group (here an attribute of Holder) sets one attribute of the group
        assertThat(source(files, "HolderTest")).contains("return new Contact(\"value\", null, null);");
    }
}
