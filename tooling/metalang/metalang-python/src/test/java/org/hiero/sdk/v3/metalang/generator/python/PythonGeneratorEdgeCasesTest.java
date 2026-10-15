package org.hiero.sdk.v3.metalang.generator.python;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.junit.jupiter.api.Test;

/** The forms the main test leaves out: literals, rarer types, and the reasons a declaration is deferred. */
class PythonGeneratorEdgeCasesTest {

    private static LinkedModel model(final Map<String, String> schemas) {
        final Map<String, String> documents = new TreeMap<>();
        schemas.forEach((file, schema) -> documents.put(file, TestSpecs.markdown(schema)));
        return LinkedModel.of(new MetaLang().validate(documents).model());
    }

    private static String module(final List<GeneratedFile> files, final String suffix) {
        return files.stream().filter(f -> f.path().endsWith(suffix)).findFirst()
                .orElseThrow(() -> new AssertionError("no " + suffix)).content();
    }

    @Test
    void literalsAndRareTypesShouldHaveTheirPythonForm() {
        final List<GeneratedFile> files = new PythonGenerator().generate(model(Map.of("f/a.md", """
                namespace a
                Values {
                    @@immutable @@default(["x"]) one: list<string>
                    @@immutable @@default(["x", "y"]) two: list<string>
                    @@immutable @@default([1, 2]) codes: set<int32>
                    @@immutable @@default([1]) bytesOne: bytes
                    @@immutable @@default([1, 2]) bytesTwo: bytes
                    @@immutable @@nullable @@default(null) gone: string
                    @@immutable @@default(2.5) rate: double
                    @@immutable day: date
                    @@immutable at: time
                    @@immutable local: dateTime
                    @@immutable kind: type<Values>
                    @@immutable anyKind: type
                    @@immutable run: function<void apply()>
                    @@immutable big: uint256
                }
                """)));
        assertThat(module(files, "/_values.py"))
                .contains("    one: tuple[str, ...] = (\"x\",)\n")
                .contains("    two: tuple[str, ...] = (\"x\", \"y\")\n")
                .contains("    codes: frozenset[int] = frozenset({1, 2})\n")
                .contains("    bytes_one: bytes = bytes((1,))\n")
                .contains("    bytes_two: bytes = bytes((1, 2))\n")
                .contains("    gone: str | None = None\n")
                .contains("    rate: float = 2.5\n")
                .contains("    day: _dt.date\n")
                .contains("    at: _dt.time\n")
                .contains("    local: _dt.datetime\n")
                .contains("    kind: type[Values]\n")
                .contains("    any_kind: type[object]\n")
                .contains("    run: Callable[[], None]\n")
                .contains("    big: int\n");
    }

    @Test
    void literalsWithoutPythonFormShouldDeferTheDeclaration() {
        final PythonGenerator generator = new PythonGenerator();
        generator.generate(model(Map.of("f/a.md", """
                namespace a
                Point { @@immutable x: int32 }
                Struct { @@immutable @@default(Point{x: 1}) p: Point }
                UnknownName { @@immutable @@default(SOMETHING) s: string }
                ListForNumber { @@immutable @@default([]) n: int32 }
                ListForPoint { @@immutable @@default([]) p: Point }
                NumberForString { @@immutable @@default(1) s: string }
                """)));
        assertThat(generator.deferred()).containsOnlyKeys(q("Struct"), q("UnknownName"), q("ListForNumber"),
                q("ListForPoint"), q("NumberForString"));
        assertThat(generator.deferred().values()).allMatch(reason -> reason.contains("Python form")
                || reason.contains("no value of"));
    }

    @Test
    void sealedAbstractionsShouldResolveTheirSubtypes() {
        final PythonGenerator generator = new PythonGenerator();
        final List<GeneratedFile> files = generator.generate(model(Map.of(
                "f/a.md", """
                        namespace a
                        requires {Far} from b
                        @@sealed(Near, b.Far)
                        abstraction Shape {}
                        Near extends Shape { @@immutable n: int32 }
                        @@sealed(Near)
                        abstraction Generic<$$T> {}
                        """,
                "f/b.md", """
                        namespace b
                        requires {Shape} from a
                        Far extends Shape { @@immutable f: int32 }
                        """)));
        assertThat(module(files, "/a/_shape.py")).contains("type Shape = Near | Far\n")
                .contains("from hiero_sdk.b._far import Far\n");
        // a generic abstraction keeps its parameters although the union of its concrete subtypes does not use them
        assertThat(module(files, "/a/_generic.py")).contains("type Generic[T] = Near\n");
        assertThat(generator.deferred()).isEmpty();
    }

    @Test
    void typesWithTheSameModuleNameShouldBeDeferred() {
        final PythonGenerator generator = new PythonGenerator();
        generator.generate(model(Map.of("f/a.md", """
                namespace a
                HTTPClient { @@immutable x: int32 }
                HttpClient { @@immutable y: int32 }
                """)));
        assertThat(generator.deferred()).hasSize(1);
        assertThat(generator.deferred().values().iterator().next()).contains("same module name");
    }

    @Test
    void structuredDocumentationShouldBeKept() {
        final String doc = PyDoc.docstring("", "Steps:\n\n* one\n* two\n\n1. first\n2. second\n\n| a | b |");
        assertThat(doc).contains("\n* one\n* two\n").contains("\n1. first\n2. second\n").contains("\n| a | b |\n");
    }

    @Test
    void importsShouldMergeRuntimeAndTypeCheckingNamesOfOneModule() {
        final PyImports imports = new PyImports("s.a._x", Set.of("X"));
        imports.sdk("s.b._y", "Y", false);
        imports.sdk("s.b._y", "Z", true);
        imports.standard("typing", "Any");
        imports.standard("typing", "TYPE_CHECKING");
        assertThat(imports.render()).contains("from typing import TYPE_CHECKING, Any\n")
                .contains("\nfrom s.b._y import Z\n").contains("\nif TYPE_CHECKING:\n    from s.b._y import Y\n");
    }

    private static QualifiedName q(final String name) {
        return new QualifiedName("a", name);
    }
}
