package org.hiero.sdk.v3.metalang.generator.ts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
 * attributes are checked, groups of mutable attributes are not; the generated tests build valid values and check the
 * rejections.
 */
class TsOneOfTest {

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
        return new TsGenerator().generate(LinkedModel.of(new MetaLang().validate(Map.of("f/a.md",
                TestSpecs.markdown(SCHEMA))).model()));
    }

    @Test
    void theConstructorsShouldCheckTheGroupsOfImmutableAttributes() {
        final List<GeneratedFile> files = generate();
        assertThat(TsGeneratorTest.file(files, "/a/Contact.ts")).contains("""
                        if ((init.email != null ? 1 : 0) + (init.phone != null ? 1 : 0) !== 1) {
                            throw new RangeError("exactly one of email, phone must be set");
                        }
                """).contains("@throws RangeError if an attribute violates its constraints");
        assertThat(TsGeneratorTest.file(files, "/a/Pick.ts")).contains("""
                        if ((init.first != null ? 1 : 0) + (init.second != null ? 1 : 0) > 1) {
                            throw new RangeError("at most one of first, second may be set");
                        }
                """);
        assertThat(TsGeneratorTest.file(files, "/a/Mark.ts"))
                .contains("if ((init.code != null ? 1 : 0) + (init.name != null ? 1 : 0) !== 1) {");
        // mutable attributes are set one by one: the group can only hold when the object is used
        assertThat(TsGeneratorTest.file(files, "/a/Pending.ts")).doesNotContain("RangeError");
    }

    @Test
    void theGeneratedValuesAndTestsShouldRespectTheGroups() throws Exception {
        final List<GeneratedFile> files = generate();
        assertThat(TsGeneratorTest.file(files, "/a/Contact.test.ts"))
                // init(): the first attribute of the group is set, the second is left out (null)
                .contains("    return {\n        email: emailValue(),\n        note: noteValue(),\n    };\n")
                .contains("test(\"rejects email and phone together (@@oneOf)\"")
                .contains("test(\"rejects none of email, phone (@@oneOf)\"");
        assertThat(TsGeneratorTest.file(files, "/a/Pick.test.ts"))
                .contains("test(\"rejects first and second together (@@oneOrNoneOf)\"")
                // a value of one attribute of the group makes the others null
                .contains("new Pick({ ...init(), first: null, second: value })")
                .doesNotContain("rejects none of");
        // a value of a type with a group (here an attribute of Holder) sets one attribute of the group
        assertThat(TsGeneratorTest.file(files, "/a/Holder.test.ts")).contains("new Contact({ email: \"");

        assumeTrue(GeneratedTs.available(), "node and generated/ts/node_modules (npm install) are needed");
        assertThat(GeneratedTs.build(files, temp, "@hiero")).isEmpty();
        assertThat(GeneratedTs.test(temp).failures()).isEmpty();
    }
}
