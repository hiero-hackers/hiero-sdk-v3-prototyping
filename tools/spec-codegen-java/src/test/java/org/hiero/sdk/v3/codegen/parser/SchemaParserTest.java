package org.hiero.sdk.v3.codegen.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.hiero.sdk.v3.codegen.model.CollectionTypeReference;
import org.hiero.sdk.v3.codegen.model.EnumDeclaration;
import org.hiero.sdk.v3.codegen.model.NamedTypeReference;
import org.hiero.sdk.v3.codegen.model.NamespaceOperationDeclaration;
import org.hiero.sdk.v3.codegen.model.TypeDeclaration;
import org.hiero.sdk.v3.codegen.model.TypeVariableReference;
import org.junit.jupiter.api.Test;

class SchemaParserTest {

    private final SchemaParser parser = new SchemaParser();

    @Test
    void parsesImportsGenericsInheritanceAnnotationsEnumsConstantsAndComments() {
        var document = parser.parse(schema("""
                namespace sample
                requires {External, Other} from shared.types

                // Base documentation.
                @@sealed(Child)
                abstraction Base<$$T extends External> {
                    @@immutable values: list<$$T> // Immutable values.
                    $$T select(index: int32)
                }

                @@finalType
                Child extends Base<External> {
                    @@immutable @@nullable other: Other
                }

                enum Mode extends External {
                    FIRST,
                    SECOND // second mode

                    @@immutable label: string
                    bool supports(value: Other)
                }

                constant DEFAULT_NAME:string = "sample"
                """));

        assertEquals("sample", document.namespace());
        assertEquals(List.of("External", "Other"), document.imports().getFirst().typeNames());
        assertEquals("shared.types", document.imports().getFirst().namespace());

        var base = assertInstanceOf(TypeDeclaration.class, document.declarations().get(0));
        assertEquals("Base", base.name());
        assertEquals("Base documentation.", base.documentation());
        assertEquals("sealed", base.annotations().getFirst().name());
        assertEquals("T", base.typeParameters().getFirst().name());
        assertInstanceOf(NamedTypeReference.class, base.typeParameters().getFirst().bound());
        assertInstanceOf(CollectionTypeReference.class, base.fields().getFirst().type());
        assertInstanceOf(TypeVariableReference.class, base.methods().getFirst().returnType());

        var mode = assertInstanceOf(EnumDeclaration.class, document.declarations().get(2));
        assertEquals(List.of("FIRST", "SECOND"), mode.values().stream().map(value -> value.name()).toList());
        assertEquals("second mode", mode.values().get(1).documentation());
        assertEquals(1, mode.fields().size());
        assertEquals(1, mode.methods().size());
    }

    @Test
    void parsesRecursiveBoundsOverloadsAndVarargs() {
        var document = parser.parse(schema("""
                namespace generic

                abstraction Token<$$Self extends Token<$$Self, $$Unit>, $$Unit> {}

                @@static Token<string, string> of(values: string...)
                @@throws(illegal-format) @@static Token<string, string> of(name: string, values: string...)
                """));

        var token = assertInstanceOf(TypeDeclaration.class, document.declarations().getFirst());
        var bound = assertInstanceOf(NamedTypeReference.class, token.typeParameters().getFirst().bound());
        assertEquals(2, bound.arguments().size());
        assertTrue(bound.arguments().stream().allMatch(TypeVariableReference.class::isInstance));

        var operations = document.declarations().stream()
                .filter(NamespaceOperationDeclaration.class::isInstance)
                .map(NamespaceOperationDeclaration.class::cast)
                .toList();
        assertEquals(2, operations.size());
        assertEquals("of", operations.getFirst().name());
        assertTrue(operations.getFirst().method().parameters().getFirst().varargs());
        assertEquals("throws", operations.get(1).annotations().getFirst().name());
        assertEquals("illegal-format", operations.get(1).annotations().getFirst().arguments().getFirst());
    }

    @Test
    void lexerRetainsCommentsNewlinesAndPreciseLocations() {
        var tokens = new SchemaLexer(schema("""
                namespace demo
                // retained
                @@immutable value: list<string>
                """)).tokenize();

        assertTrue(tokens.stream().anyMatch(token -> token.type() == TokenType.COMMENT
                && token.lexeme().equals("retained")));
        assertTrue(tokens.stream().anyMatch(token -> token.type() == TokenType.NEWLINE));
        var annotation = tokens.stream()
                .filter(token -> token.type() == TokenType.ANNOTATION)
                .findFirst()
                .orElseThrow();
        assertEquals("immutable", annotation.lexeme());
        assertEquals("spec/base/test.md:3:1", annotation.location().display());
    }

    private static ExtractedSchema schema(String content) {
        return new ExtractedSchema("spec/base/test.md", content, 1);
    }
}
