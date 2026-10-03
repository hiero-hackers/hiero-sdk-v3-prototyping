package org.hiero.sdk.v3.metalang.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;
import org.junit.jupiter.api.Test;

class ConstraintsTest {

    private static final SourceLocation AT = new SourceLocation("a.md", 1, 1);

    private static Annotation number(final String name, final String value) {
        return new Annotation(name, List.of(new Literal.NumberLiteral(value, AT)), true, AT);
    }

    private static Annotation string(final String name, final String value) {
        return new Annotation(name, List.of(new Literal.StringLiteral(value, AT)), true, AT);
    }

    private static BuiltinType builtin(final String name) {
        return BuiltinType.lookup(name).orElseThrow();
    }

    @Test
    void shouldIntersectTheIntegerRangeWithMinAndMax() {
        assertThat(Constraints.range(builtin("uint8"), List.of())).isEqualTo(new IntegerRange(BigInteger.ZERO,
                BigInteger.valueOf(255)));
        assertThat(Constraints.range(builtin("uint8"), List.of(number("min", "1.5"), number("max", "9.5"))))
                .isEqualTo(new IntegerRange(BigInteger.TWO, BigInteger.valueOf(9)));
        assertThat(Constraints.range(builtin("int8"), List.of(number("min", "5"), number("max", "1"))).min())
                .isGreaterThan(BigInteger.ONE);
    }

    @Test
    void shouldChooseDecimalsWithinTheBounds() {
        assertThat(Constraints.decimal(List.of(), 1)).contains(new BigDecimal("2.5"));
        assertThat(Constraints.decimal(List.of(number("min", "10")), 0)).contains(BigDecimal.TEN);
        assertThat(Constraints.decimal(List.of(number("max", "1")), 0)).contains(BigDecimal.ONE);
        assertThat(Constraints.decimal(List.of(number("min", "3"), number("max", "1")), 0)).isEmpty();
    }

    @Test
    void shouldBuildStringsThatFulfilTheConstraints() {
        assertThat(Constraints.string(List.of(), 0)).contains("value");
        assertThat(Constraints.string(List.of(), 2)).contains("value2");
        assertThat(Constraints.string(List.of(number("minLength", "8")), 0)).contains("valueaaa");
        assertThat(Constraints.string(List.of(number("maxLength", "2")), 0)).contains("va");
        assertThat(Constraints.string(List.of(string("pattern", "^/[a-z]*$")), 0)).contains("/");
        assertThat(Constraints.string(List.of(new Annotation("urlPattern", List.of(), false, AT)), 1))
                .contains("https://example.com/v1");
        assertThat(Constraints.string(List.of(string("pattern", "^[0-9]{3}$"), number("maxLength", "2")), 0))
                .isEmpty();
        assertThat(Constraints.isValidString("not a url", List.of(new Annotation("urlPattern", List.of(), false,
                AT)))).isFalse();
        assertThat(Constraints.isValidString("mailto:x", List.of(new Annotation("urlPattern", List.of(), false,
                AT)))).isFalse();
        assertThat(Constraints.isValidString("a", List.of(number("minLength", "2")))).isFalse();
        assertThat(Constraints.isValidString("abc", List.of(number("maxLength", "2")))).isFalse();
    }

    @Test
    void shouldCountElementsWithinTheSizeBounds() {
        assertThat(Constraints.elementCount(List.of(), 1)).isEqualTo(1);
        assertThat(Constraints.elementCount(List.of(number("minSize", "4")), 1)).isEqualTo(4);
        assertThat(Constraints.elementCount(List.of(number("maxSize", "0")), 1)).isZero();
        assertThat(Constraints.elementCount(List.of(number("minSize", "2"), number("maxSize", "1")), 1))
                .isEqualTo(-1);
        assertThat(Constraints.size(List.of(number("minSize", "3")), "minSize")).contains(3);
        assertThat(Constraints.bound(List.of(string("min", "x")), "min")).isEmpty();
        assertThat(Constraints.stringArgument(List.of(number("pattern", "5")), "pattern")).contains("5");
    }

    @Test
    void shouldChooseTypeArgumentsAndMatchDefaultInstances() {
        // GIVEN
        final LinkedModel model = LinkedModel.of(new MetaLang().validate(Map.of("f/a.md", TestSpecs.markdown("""
                namespace a
                abstraction Unit { @@immutable symbol: string }
                Box<$$T, $$U extends Unit> { @@immutable t: $$T
                    @@immutable u: $$U }
                Self<$$S extends Self<$$S>> { @@immutable s: $$S }
                """))).model());
        final TypeDefinition box = model.type(new QualifiedName("a", "Box")).orElseThrow();
        final TypeDefinition self = model.type(new QualifiedName("a", "Self")).orElseThrow();
        final Type unit = new Type.DeclaredType(new QualifiedName("a", "Unit"), List.of());

        // THEN
        assertThat(Constraints.defaults(box.typeParameters())).hasValueSatisfying(m -> assertThat(m.values())
                .containsExactlyInAnyOrder(Constraints.STRING, unit));
        assertThat(Constraints.defaults(self.typeParameters())).isEmpty();
        assertThat(Constraints.arguments(box, new Type.DeclaredType(box.name(), List.of(Constraints.STRING,
                new Type.WildcardType(null))))).hasValueSatisfying(m -> assertThat(m.values())
                .containsExactlyInAnyOrder(Constraints.STRING, unit));
        assertThat(Constraints.arguments(self, new Type.DeclaredType(self.name(), List.of()))).isEmpty();
        final Type.DeclaredType anyBox = new Type.DeclaredType(box.name(), List.of(new Type.AnyType(),
                new Type.AnyType()));
        assertThat(Constraints.fitsInstance(anyBox, new Type.DeclaredType(box.name(), List.of(new Type.AnyType(),
                new Type.WildcardType(null))))).isTrue();
        assertThat(Constraints.fitsInstance(anyBox, new Type.DeclaredType(box.name(), List.of(Constraints.STRING,
                unit)))).isFalse();
        assertThat(Constraints.fitsInstance(new Type.DeclaredType(box.name(), List.of()),
                new Type.DeclaredType(box.name(), List.of(Constraints.STRING)))).isFalse();
    }
}
