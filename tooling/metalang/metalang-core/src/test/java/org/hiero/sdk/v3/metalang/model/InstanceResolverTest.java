package org.hiero.sdk.v3.metalang.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.ValidationReport;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.junit.jupiter.api.Test;

class InstanceResolverTest {

    private static final String SCHEMA = """
            namespace a
            requires {Remote} from b
            enum Color { RED GREEN }
            abstraction Shape { void draw() }
            Circle extends Shape { @@immutable radius: double
                void draw() }
            Box<$$T> { @@immutable value: $$T }
            Point {
                @@immutable x: int32
                @@immutable @@nullable label: string
                @@immutable @@default(0) z: int64
                @@immutable data: bytes
                @@immutable tags: list<string>
                @@immutable color: Color
                @@immutable flag: bool
                Point moved(dx: int32)
                @@static Point origin()
            }
            Holder { @@immutable point: Point
                @@immutable remote: Remote }
            constant ORIGIN_X: int32 = 0
            @@static Point create(x: int32)
            @@static Point create(name: string)
            @@static Point pick(value: int32)
            @@static Point pick(value: string)
            """;

    private static final String OTHER = """
            namespace b
            Remote { @@immutable id: int32 }
            @@static Remote connect(id: int32)
            constant DEFAULT_ID: int32 = 7
            """;

    private static String markdown(final String schema, final String instances) {
        return "# T\n\n## Description\n\nT.\n\n## API Schema\n\n```\n" + schema + "```\n\n"
                + (instances == null ? "" : "## Default Instances\n\n```\n" + instances
                + (instances.endsWith("\n") ? "" : "\n") + "```\n\n")
                + "## Testing\n\nN.\n\n## Questions & Comments\n";
    }

    private static ValidationReport validate(final String instances) {
        final Map<String, String> documents = new TreeMap<>();
        documents.put("f/a.md", markdown(SCHEMA, instances));
        documents.put("f/b.md", markdown(OTHER, null));
        return new MetaLang().validate(documents);
    }

    private static List<String> problems(final String instances) {
        return validate(instances).diagnostics().stream()
                .filter(d -> d.ruleId().startsWith("instance.") && !d.ruleId().equals("instance.missing"))
                .map(Diagnostic::message).toList();
    }

    private static InstanceExpression expression(final String instances, final String type) {
        final LinkedModel model = LinkedModel.of(validate(instances).model());
        return model.instance(new QualifiedName("a", type)).orElseThrow().expression();
    }

    @Test
    void shouldResolveAllKindsOfExpressions() {
        // WHEN
        final String instances = """
                // the origin
                instance Point = Point{x: ORIGIN_X, label: null, data: [1, 2], tags: ["a"], color: RED, flag: true}
                instance Circle = Circle{radius: 1.5}
                instance Shape = DEFAULT(Circle)
                instance Holder = Holder{point: DEFAULT(Point).moved(dx: 2), remote: b.connect(id: b.DEFAULT_ID)}
                instance Box<ANY> = Box<string>{value: "x"}
                instance Color = Color.GREEN
                """;
        final ValidationReport report = validate(instances);
        final LinkedModel model = LinkedModel.of(report.model());

        // THEN
        assertThat(problems(instances)).isEmpty();
        assertThat(model.instances()).extracting(i -> i.type().text()).containsExactly("a.Point", "a.Circle",
                "a.Shape", "a.Holder", "a.Box<ANY>", "a.Color");
        assertThat(model.instance(new QualifiedName("a", "Point")).orElseThrow().documentation())
                .isEqualTo("the origin");
        final InstanceExpression.Construct point = (InstanceExpression.Construct) expression(instances, "Point");
        assertThat(point.values().keySet()).containsExactly("x", "label", "data", "tags", "color", "flag");
        assertThat(point.values().get("x")).isInstanceOf(InstanceExpression.ConstantValue.class);
        assertThat(point.values().get("data")).isInstanceOfSatisfying(InstanceExpression.ListValue.class,
                l -> assertThat(l.items()).hasSize(2));
        assertThat(point.values().get("color")).isInstanceOfSatisfying(InstanceExpression.Value.class,
                v -> assertThat(v.literal().text()).isEqualTo("Color.RED"));
        final InstanceExpression.Construct holder = (InstanceExpression.Construct) expression(instances, "Holder");
        assertThat(holder.values().get("point")).isInstanceOfSatisfying(InstanceExpression.MethodCall.class,
                c -> assertThat(c.method().name()).isEqualTo("moved"));
        assertThat(holder.values().get("remote")).isInstanceOfSatisfying(InstanceExpression.Call.class, c -> {
            assertThat(c.owner()).isNull();
            assertThat(c.namespace()).isEqualTo("b");
            assertThat(c.arguments().get("id")).isInstanceOf(InstanceExpression.ConstantValue.class);
        });
        assertThat(expression(instances, "Shape")).isEqualTo(new InstanceExpression.Default(
                new Type.DeclaredType(new QualifiedName("a", "Circle"), List.of())));
        assertThat(((InstanceExpression.Construct) expression(instances, "Box")).values().get("value").type().text())
                .isEqualTo("string");
    }

    @Test
    void shouldSelectOverloadsAndStaticMethods() {
        // WHEN
        final String instances = """
                instance Point = create(name: "p")
                instance Box<ANY> = Box<int32>{value: DEFAULT(Point).moved(dx: 1).x}
                """;

        // THEN
        assertThat(problems(instances)).isEmpty();
        assertThat(expression(instances, "Point")).isInstanceOfSatisfying(InstanceExpression.Call.class,
                c -> assertThat(c.method().signature()).isEqualTo("create(string)"));
        assertThat(problems("instance Point = Point.origin()")).isEmpty();
        assertThat(expression("instance Point = Point.origin()", "Point"))
                .isInstanceOfSatisfying(InstanceExpression.Call.class,
                        c -> assertThat(c.owner()).isEqualTo(new QualifiedName("a", "Point")));
    }

    @Test
    void shouldReportWhatCannotBeResolved() {
        assertThat(problems("instance Point = unknown(x: 1)"))
                .containsExactly("Unknown function 'unknown' in namespace 'a'");
        assertThat(problems("instance Point = Point.unknown()")).containsExactly("Unknown static method 'Point.unknown'");
        assertThat(problems("instance Point = create(y: 1)")).containsExactly("No overload of function 'create' in "
                + "namespace 'a' fits the arguments (y): create(int32), create(string)");
        assertThat(problems("instance Point = pick(value: DEFAULT)")).singleElement().asString()
                .startsWith("More than one overload of function 'pick'");
        assertThat(problems("instance Point = b.connect(id: \"x\")")).containsExactly(
                "A string is no 'int32'");
        assertThat(problems("instance Point = b.connect()")).containsExactly(
                "function 'b.connect' needs a value for 'id'");
        assertThat(problems("instance Point = Point{x: 1, x: 2, data: [], tags: [], color: RED, flag: false}"))
                .containsExactly("'x' is given twice");
        assertThat(problems("instance Shape = Shape{}")).containsExactly(
                "'Shape' is no complex type that can be created (abstractions and enums cannot)");
        assertThat(problems("instance Shape = DEFAULT.x")).containsExactly("DEFAULT needs a type here: DEFAULT(Type)");
        assertThat(problems("instance Shape = DEFAULT(Unknown)")).containsExactly("Unknown type '?Unknown'");
        assertThat(problems("instance Point = DEFAULT(Circle).moved(dx: 1)"))
                .containsExactly("Unknown method 'moved' of 'a.Circle'");
        assertThat(problems("instance Point = DEFAULT(string).moved(dx: 1)"))
                .containsExactly("A 'string' has no methods");
        assertThat(problems("instance Point = DEFAULT(Circle).center")).containsExactly(
                "A 'a.Circle' has no attribute 'center'");
        assertThat(problems("instance Circle = Circle{radius: \"x\"}")).containsExactly("A string is no 'double'");
        assertThat(problems("instance Circle = Circle{radius: true}")).containsExactly("true is no 'double'");
        assertThat(problems("instance Circle = Circle{radius: GREEN}")).containsExactly(
                "'GREEN' is no constant and no enum constant");
        assertThat(problems("instance Color = 1")).containsExactly("The number 1 is no 'a.Color'");
        assertThat(problems("instance Box<ANY> = Box<int32>{value: 1.5}")).containsExactly(
                "The number 1.5 is no 'int32'");
        assertThat(problems("instance Circle = Circle{radius: [1]}")).containsExactly(
                "A list is used for a list, a set or bytes, not for 'double'");
        assertThat(problems("instance Point = Point{x: 1, data: [\"x\"], tags: [], color: RED, flag: false}"))
                .containsExactly("A string is no 'uint8'");
        assertThat(problems("instance Shape = DEFAULT(Point)")).containsExactly(
                "The value is a 'a.Point', but a 'a.Shape' is expected");
        assertThat(problems("instance b.Remote = b.connect(id: 1)")).containsExactly(
                "The default instance of 'b.Remote' belongs to the spec of namespace 'b'");
        assertThat(problems("instance Unknown = 1")).containsExactly(
                "A default instance needs a type declared in the specs, not 'Unknown'");
        assertThat(problems("instance Color = RED\ninstance Color = GREEN")).singleElement().asString()
                .startsWith("'a.Color' already has a default instance (f/a.md:");
    }

    @Test
    void shouldReportCyclesAndSyntaxErrors() {
        assertThat(problems("instance Holder = Holder{point: DEFAULT, remote: DEFAULT(Holder).remote}"))
                .containsExactly("The default instance of 'a.Holder' depends on itself through DEFAULT");
        final List<Diagnostic> syntax = validate("instance Point = Point{x: }").diagnostics().stream()
                .filter(d -> d.ruleId().equals("syntax.error")).toList();
        // the line in the Markdown file
        final int line = markdown(SCHEMA, "instance Point = Point{x: }").lines().toList()
                .indexOf("instance Point = Point{x: }") + 1;
        assertThat(syntax).singleElement().satisfies(d -> assertThat(d.location().line()).isEqualTo(line));
    }
}
