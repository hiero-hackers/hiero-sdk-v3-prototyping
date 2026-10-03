package org.hiero.sdk.v3.metalang;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.EnumValue;
import org.hiero.sdk.v3.metalang.ast.Field;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.ast.Method;
import org.hiero.sdk.v3.metalang.ast.Node;
import org.hiero.sdk.v3.metalang.ast.Parameter;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.TypeRef;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.parser.SchemaParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Robustness and consistency checks against the real specs of this repository:
 *
 * <ul>
 *   <li>deterministic random mutations and truncations never crash the tool (validation and linking) and never
 *       produce diagnostics outside the document,</li>
 *   <li>the result does not depend on the order in which documents are passed in,</li>
 *   <li>source locations of AST nodes point at the element in the Markdown file,</li>
 *   <li>the textual form of every type reference and literal parses back to the same text.</li>
 * </ul>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RobustnessTest {

    private static final Path SPEC_ROOT = Path.of(System.getProperty("spec.root", "../../spec"));
    private static final int MUTATIONS_PER_SPEC = 40;
    private static final String INSERTABLE = "{}()<>,:;@$.\"/*-=[] \nXa1";

    private final SortedMap<String, String> documents = new TreeMap<>();

    @BeforeAll
    void loadSpecs() throws IOException {
        try (Stream<Path> files = Files.walk(SPEC_ROOT)) {
            for (final Path file : files.filter(p -> p.toString().endsWith(".md")).sorted().toList()) {
                documents.put(SPEC_ROOT.relativize(file).toString().replace('\\', '/'),
                        Files.readString(file, StandardCharsets.UTF_8));
            }
        }
        assertThat(documents).hasSizeGreaterThan(40);
    }

    private static void assertSane(final String name, final String markdown) {
        final int lines = Math.max(1, markdown.split("\\R").length);  // trailing line breaks start no new line
        final ValidationReport[] report = new ValidationReport[1];
        assertThatCode(() -> report[0] = new MetaLang().validate(Map.of(name, markdown)))
                .as("crash for mutated " + name).doesNotThrowAnyException();
        assertThatCode(() -> LinkedModel.of(report[0].model()))
                .as("linking crashed for mutated " + name).doesNotThrowAnyException();
        assertThat(report[0].diagnostics()).allSatisfy(d -> {
            assertThat(d.location().file()).isEqualTo(name);
            assertThat(d.location().line()).as(d.toString()).isBetween(1, lines);
            assertThat(d.location().column()).as(d.toString()).isPositive();
        });
    }

    private static String mutate(final String text, final Random random) {
        final List<String> lines = new ArrayList<>(List.of(text.split("\n", -1)));
        final int line = random.nextInt(lines.size());
        final String current = lines.get(line);
        final int column = current.isEmpty() ? 0 : random.nextInt(current.length() + 1);
        switch (random.nextInt(6)) {
            case 0 -> lines.set(line, current.isEmpty() ? current
                    : current.substring(0, Math.min(column, current.length() - 1))
                    + current.substring(Math.min(column, current.length() - 1) + 1));
            case 1 -> lines.remove(line);
            case 2 -> lines.add(line, current);
            case 3 -> lines.set(line, current.substring(0, column)
                    + INSERTABLE.charAt(random.nextInt(INSERTABLE.length())) + current.substring(column));
            case 4 -> {
                if (line + 1 < lines.size()) {
                    Collections.swap(lines, line, line + 1);
                }
            }
            default -> {
                return String.join("\n", lines).substring(0, random.nextInt(text.length() + 1));
            }
        }
        return String.join("\n", lines);
    }

    @Test
    void randomMutationsShouldNeverCrashTheTool() {
        for (final Map.Entry<String, String> document : documents.entrySet()) {
            // seeded per file, so every failure is reproducible
            final Random random = new Random(document.getKey().hashCode());
            String mutated = document.getValue();
            for (int i = 0; i < MUTATIONS_PER_SPEC; i++) {
                mutated = mutate(i % 5 == 0 ? document.getValue() : mutated, random);
                assertSane(document.getKey(), mutated);
            }
        }
    }

    @Test
    void everyPrefixOfASpecShouldBeHandled() {
        for (final Map.Entry<String, String> document : documents.entrySet()) {
            final String[] lines = document.getValue().split("\n", -1);
            final StringBuilder prefix = new StringBuilder();
            for (final String line : lines) {
                prefix.append(line).append('\n');
                assertSane(document.getKey(), prefix.toString());
            }
        }
    }

    @Test
    void resultShouldNotDependOnDocumentOrder() {
        // GIVEN
        final List<String> keys = new ArrayList<>(documents.keySet());
        Collections.shuffle(keys, new Random(42));
        final Map<String, String> shuffled = new LinkedHashMap<>();
        keys.forEach(k -> shuffled.put(k, documents.get(k)));
        Collections.reverse(keys);
        final Map<String, String> reversed = new LinkedHashMap<>();
        keys.forEach(k -> reversed.put(k, documents.get(k)));

        // WHEN
        final List<Diagnostic> expected = new MetaLang().validate(documents).diagnostics();

        // THEN
        assertThat(new MetaLang().validate(shuffled).diagnostics()).isEqualTo(expected);
        assertThat(new MetaLang().validate(reversed).diagnostics()).isEqualTo(expected);
    }

    private void forEachNamedNode(final SchemaFile file, final Consumer<Map.Entry<String, Node>> visitor) {
        for (final Declaration declaration : file.declarations()) {
            switch (declaration) {
                case Declaration.TypeDeclaration type -> {
                    visitor.accept(Map.entry(type.name(), type));
                    type.fields().forEach(f -> visitor.accept(Map.entry(f.name(), f)));
                    type.methods().forEach(m -> visitMethod(m, visitor));
                    if (type instanceof Declaration.EnumType enumType) {
                        enumType.values().forEach(v -> visitor.accept(Map.entry(v.name(), v)));
                        enumType.attributes().forEach(a -> visitor.accept(Map.entry(a.name(), a)));
                    }
                }
                case Declaration.Function function -> visitMethod(function.method(), visitor);
                case Declaration.Constant constant -> visitor.accept(Map.entry("constant", constant));
            }
        }
    }

    private static void visitMethod(final Method method, final Consumer<Map.Entry<String, Node>> visitor) {
        visitor.accept(Map.entry(method.name(), method));
        method.parameters().forEach(p -> visitor.accept(Map.entry(p.name(), p)));
    }

    @Test
    void sourceLocationsShouldPointAtTheElementInTheMarkdownFile() {
        final ValidationReport report = new MetaLang().validate(documents);
        int checked = 0;
        for (final SchemaFile file : report.model().files()) {
            final String[] lines = documents.get(file.file()).split("\\R", -1);
            final List<String> mismatches = new ArrayList<>();
            forEachNamedNode(file, entry -> {
                final Node node = entry.getValue();
                final String line = lines[node.location().line() - 1];
                if (!line.startsWith(entry.getKey(), node.location().column() - 1)) {
                    mismatches.add(entry.getKey() + " @ " + node.location() + ": " + line.strip());
                }
            });
            assertThat(mismatches).as(file.file()).isEmpty();
            checked++;
        }
        assertThat(checked).isGreaterThan(40);
    }

    private static List<String> typeTexts(final SchemaFile file) {
        final List<String> texts = new ArrayList<>();
        for (final Declaration declaration : file.declarations()) {
            switch (declaration) {
                case Declaration.TypeDeclaration type -> {
                    type.supertypes().forEach(t -> texts.add(t.text()));
                    type.fields().stream().map(Field::type).forEach(t -> texts.add(t.text()));
                    type.methods().forEach(m -> addMethodTypes(m, texts));
                    if (type instanceof Declaration.EnumType enumType) {
                        enumType.attributes().stream().map(Parameter::type).forEach(t -> texts.add(t.text()));
                    }
                }
                case Declaration.Function function -> addMethodTypes(function.method(), texts);
                case Declaration.Constant constant -> texts.add(constant.type().text());
            }
        }
        return texts;
    }

    private static void addMethodTypes(final Method method, final List<String> texts) {
        texts.add(method.returnType().text());
        method.parameters().stream().map(Parameter::type).map(TypeRef::text).forEach(texts::add);
    }

    private static List<Literal> literals(final SchemaFile file) {
        final List<Literal> literals = new ArrayList<>();
        final Consumer<List<Annotation>> fromAnnotations = annotations ->
                annotations.forEach(a -> literals.addAll(a.arguments()));
        for (final Declaration declaration : file.declarations()) {
            fromAnnotations.accept(declaration.annotations());
            if (declaration instanceof Declaration.Constant constant) {
                literals.add(constant.value());
            }
            if (declaration instanceof Declaration.TypeDeclaration type) {
                type.fields().forEach(f -> fromAnnotations.accept(f.annotations()));
                type.methods().forEach(m -> fromAnnotations.accept(m.annotations()));
            }
            if (declaration instanceof Declaration.EnumType enumType) {
                enumType.values().stream().map(EnumValue::arguments).forEach(literals::addAll);
            }
        }
        return literals;
    }

    @Test
    void textualFormsShouldParseBackToTheSameText() {
        final SchemaParser parser = new SchemaParser();
        final ValidationReport report = new MetaLang().validate(documents);
        int checked = 0;
        for (final SchemaFile file : report.model().files()) {
            for (final String text : typeTexts(file)) {
                final SchemaFile reparsed = parser.parse("roundtrip.md", "namespace x\nT { f: " + text + " }").ast()
                        .orElseThrow(() -> new AssertionError("type text does not parse: " + text));
                final Declaration.ComplexType type = (Declaration.ComplexType) reparsed.declarations().getFirst();
                assertThat(type.fields().getFirst().type().text()).isEqualTo(text);
                checked++;
            }
            for (final Literal literal : literals(file)) {
                final SchemaFile reparsed = parser.parse("roundtrip.md", "namespace x\nconstant C: T = "
                        + (literal.text().matches("[a-z][a-z0-9]*(-[a-z0-9]+)+") ? "\"" + literal.text() + "\""
                        : literal.text())).ast()
                        .orElseThrow(() -> new AssertionError("literal text does not parse: " + literal.text()));
                final Literal value = ((Declaration.Constant) reparsed.declarations().getFirst()).value();
                assertThat(value.text().replace("\"", "")).isEqualTo(literal.text().replace("\"", ""));
                checked++;
            }
        }
        assertThat(checked).isGreaterThan(500);
    }
}
