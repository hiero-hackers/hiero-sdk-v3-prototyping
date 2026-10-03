package org.hiero.sdk.v3.metalang.semantic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.parser.SchemaParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SpecModelTest {

    private static SchemaFile parse(final String file, final String text) {
        return new SchemaParser().parse(file, text).ast().orElseThrow();
    }

    private final SchemaFile a = parse("a.md", "namespace a\nrequires {B} from b\nA extends B {}\nC extends A, B {}");
    private final SchemaFile b = parse("b.md", "namespace b\nB {}");
    private final SpecModel model = SpecModel.of(List.of(b, a));

    @Test
    void shouldIndexFilesNamespacesAndTypes() {
        assertThat(model.files()).containsExactly(a, b);
        assertThat(model.namespaceNames()).containsExactly("a", "b");
        assertThat(model.filesOf("a")).containsExactly(a);
        assertThat(model.filesOf("missing")).isEmpty();
        assertThat(model.type("b", "B")).isPresent();
        assertThat(model.type("b", "X")).isEmpty();
        assertThat(model.namespacesDeclaring("B")).containsExactly("b");
        assertThat(model.fileOf(model.type("b", "B").orElseThrow())).isSameAs(b);
    }

    @Test
    void shouldRejectForeignDeclarations() {
        final Declaration foreign = parse("x.md", "namespace x\nX {}").declarations().getFirst();
        assertThatThrownBy(() -> model.fileOf(foreign)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldComputeSupertypesAndAncestors() {
        final Declaration.TypeDeclaration c = model.type("a", "C").orElseThrow();
        assertThat(model.directSupertypes(c)).extracting(d -> d.declaration().name()).containsExactly("A", "B");
        assertThat(model.ancestors(c)).extracting(Declaration::name).containsExactly("A", "B");
    }

    @Test
    void shouldResolveNames() {
        assertThat(model.resolve(a, "B")).isInstanceOf(ResolvedType.Declared.class);
        assertThat(model.resolve(a, "b.B")).isInstanceOf(ResolvedType.Declared.class);
        assertThat(model.resolve(a, "int32")).isInstanceOf(ResolvedType.Builtin.class);
        assertThat(model.resolve(a, "uint")).isEqualTo(new ResolvedType.Unresolved(Rule.TYPE_UNKNOWN,
                "Unknown type 'uint'; integer types need an explicit width, e.g. 'uint32'"));
        assertThat(model.resolve(b, "A")).isInstanceOf(ResolvedType.Unresolved.class);
    }

    @ParameterizedTest
    @CsvSource({
            "int8, INTEGER, 0, true", "uint256, INTEGER, 0, true", "int512, INTEGER, 0, false",
            "list, COLLECTION, 1, true", "map, MAP, 2, true", "streamResult, STREAM_RESULT, 1, true",
            "seconds, DURATION, 0, true", "duration, DURATION, 0, true", "zonedDateTime, TEMPORAL, 0, true"
    })
    void shouldKnowBuiltinTypes(final String name, final BuiltinType.Category category, final int arity,
                                final boolean validWidth) {
        final BuiltinType type = BuiltinType.lookup(name).orElseThrow();
        assertThat(type.category()).isEqualTo(category);
        assertThat(type.arity()).isEqualTo(arity);
        assertThat(type.hasValidWidth()).isEqualTo(validWidth);
    }

    @Test
    void shouldNotTreatOtherNamesAsBuiltins() {
        assertThat(BuiltinType.lookup("int")).isEmpty();
        assertThat(BuiltinType.lookup("int123456")).isEmpty();
        assertThat(BuiltinType.lookup("Address")).isEmpty();
        assertThat(BuiltinType.lookup("double").orElseThrow().isNumeric()).isTrue();
        assertThat(BuiltinType.lookup("set").orElseThrow().isCollection()).isTrue();
        assertThat(BuiltinType.lookup("bytes").orElseThrow().isCollection()).isFalse();
    }

    @Test
    void shouldKnowAnnotations() {
        assertThat(KnownAnnotation.byName("oneOf")).contains(KnownAnnotation.ONE_OF);
        assertThat(KnownAnnotation.byName("positiveValue")).isEmpty();
        assertThat(KnownAnnotation.STREAMING.targets()).contains(KnownAnnotation.ElementKind.METHOD);
        assertThat(KnownAnnotation.THROWS.arguments()).isEqualTo(KnownAnnotation.Arguments.NAMES);
        assertThat(KnownAnnotation.FINAL_TYPE.annotationName()).isEqualTo("finalType");
    }

    @Test
    void shouldExposeLocations() {
        assertThat(a.location()).isEqualTo(new SourceLocation("a.md", 1, 1));
    }
}
