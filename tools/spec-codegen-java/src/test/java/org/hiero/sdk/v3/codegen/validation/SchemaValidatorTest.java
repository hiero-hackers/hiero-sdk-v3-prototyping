package org.hiero.sdk.v3.codegen.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.hiero.sdk.v3.codegen.model.MappingDecision;
import org.hiero.sdk.v3.codegen.model.SchemaException;
import org.hiero.sdk.v3.codegen.parser.ExtractedSchema;
import org.hiero.sdk.v3.codegen.parser.SchemaParser;
import org.junit.jupiter.api.Test;

class SchemaValidatorTest {

    private final SchemaParser parser = new SchemaParser();
    private final SchemaValidator validator = new SchemaValidator();

    @Test
    void rejectsUnresolvedAndUnusedImports() {
        var unresolved = document("unresolved", """
                namespace unresolved
                requires {Missing} from absent
                UsesMissing { @@immutable value: Missing }
                """);
        assertCode("UNRESOLVED_NAMESPACE", () -> validator.validate(List.of(unresolved)));

        var shared = document("shared", """
                namespace shared
                External {}
                """);
        var unused = document("unused", """
                namespace unused
                requires {External} from shared
                Local {}
                """);
        assertCode("UNUSED_IMPORT", () -> validator.validate(List.of(shared, unused)));
    }

    @Test
    void rejectsDuplicateDeclarationsInvalidBoundsAndUnsupportedAnnotations() {
        var duplicate = document("duplicate", """
                namespace duplicate
                Value {}
                Value {}
                """);
        assertCode("DUPLICATE_DECLARATION", () -> validator.validate(List.of(duplicate)));

        var invalidBound = document("bound", """
                namespace bound
                abstraction Value<$$T extends $$Missing> {}
                """);
        assertCode("UNRESOLVED_TYPE_VARIABLE", () -> validator.validate(List.of(invalidBound)));

        var unsupported = document("unsupported", """
                namespace unsupported
                @@invented Value {}
                """);
        assertCode("UNSUPPORTED_ANNOTATION", () -> validator.validate(List.of(unsupported)));
    }

    @Test
    void rejectsUnmappedAndMultiplyMappedSourceElements() {
        var resolved = validator.validate(List.of(document("mapping", """
                namespace mapping
                Value { @@immutable name: string }
                """)));

        assertCode("UNMAPPED_ELEMENT", () -> validator.validateMappings(resolved, List.of()));

        var identity = "mapping#Value";
        var first = MappingDecision.direct(identity, "org.example.Value", "RULE", "FR-003");
        var second = MappingDecision.direct(identity, "org.example.OtherValue", "RULE", "FR-003");
        assertCode("MULTIPLY_MAPPED_ELEMENT", () -> validator.validateMappings(resolved, List.of(first, second)));
    }

    private org.hiero.sdk.v3.codegen.model.SchemaDocument document(String name, String content) {
        return parser.parse(new ExtractedSchema("spec/base/" + name + ".md", content, 1));
    }

    private static void assertCode(String code, org.junit.jupiter.api.function.Executable executable) {
        var exception = assertThrows(SchemaException.class, executable);
        assertEquals(code, exception.code());
    }
}
