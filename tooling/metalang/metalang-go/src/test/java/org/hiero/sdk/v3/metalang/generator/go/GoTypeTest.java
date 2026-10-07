package org.hiero.sdk.v3.metalang.generator.go;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The two properties of a Go type that drive decisions elsewhere: whether Go allows it as a map key, and whether
 * its zero value is already {@code nil}.
 */
class GoTypeTest {

    private static final GoImports IMPORTS = new GoImports("example.com/m/a");

    @Test
    void shouldKnowWhatGoAllowsAsAMapKey() {
        assertThat(new GoType.Predeclared("string").comparable()).isTrue();
        assertThat(new GoType.Library("time", "Time", true).comparable()).isTrue();
        assertThat(new GoType.Pointer(new GoType.Predeclared("int32")).comparable()).isTrue();
        assertThat(new GoType.Variable("T").comparable()).isTrue();
        // an interface is comparable at compile time, which is all a map key declaration needs
        assertThat(new GoType.Any().comparable()).isTrue();
        assertThat(new GoType.Declared("p", "X", true, true, List.of()).comparable()).isTrue();

        assertThat(new GoType.Bytes().comparable()).isFalse();
        assertThat(new GoType.Slice(new GoType.Predeclared("int32")).comparable()).isFalse();
        assertThat(new GoType.MapOf(new GoType.Predeclared("string"), GoType.MapOf.UNIT).comparable()).isFalse();
        assertThat(new GoType.Func(List.of(), new GoType.Void()).comparable()).isFalse();
        assertThat(new GoType.StreamResult(new GoType.Predeclared("string")).comparable()).isFalse();
    }

    @Test
    void shouldKnowWhoseZeroValueIsAlreadyNil() {
        // these need no pointer to express an absent value
        assertThat(new GoType.Bytes().nilable()).isTrue();
        assertThat(new GoType.Slice(new GoType.Predeclared("int32")).nilable()).isTrue();
        assertThat(new GoType.MapOf(new GoType.Predeclared("string"), GoType.MapOf.UNIT).nilable()).isTrue();
        assertThat(new GoType.Pointer(new GoType.Predeclared("int32")).nilable()).isTrue();
        assertThat(new GoType.Func(List.of(), new GoType.Void()).nilable()).isTrue();
        assertThat(new GoType.Any().nilable()).isTrue();
        assertThat(new GoType.StreamResult(new GoType.Predeclared("string")).nilable()).isTrue();
        assertThat(new GoType.Declared("p", "X", true, true, List.of()).nilable()).isTrue();

        // these do
        assertThat(new GoType.Predeclared("string").nilable()).isFalse();
        assertThat(new GoType.Library("time", "Time", true).nilable()).isFalse();
        assertThat(new GoType.Variable("T").nilable()).isFalse();
        assertThat(new GoType.Declared("p", "X", false, true, List.of()).nilable()).isFalse();
        assertThat(new GoType.Void().nilable()).isFalse();
    }

    @Test
    void shouldRenderEveryForm() {
        final GoType string = new GoType.Predeclared("string");
        assertThat(string.render(IMPORTS)).isEqualTo("string");
        assertThat(new GoType.Bytes().render(IMPORTS)).isEqualTo("[]byte");
        assertThat(new GoType.Slice(string).render(IMPORTS)).isEqualTo("[]string");
        assertThat(new GoType.MapOf(string, GoType.MapOf.UNIT).render(IMPORTS))
                .isEqualTo("map[string]struct{}");
        assertThat(new GoType.Pointer(string).render(IMPORTS)).isEqualTo("*string");
        assertThat(new GoType.Any().render(IMPORTS)).isEqualTo("any");
        assertThat(new GoType.Variable("T").render(IMPORTS)).isEqualTo("T");
        assertThat(new GoType.Void().render(IMPORTS)).isEmpty();
        assertThat(new GoType.Library("time", "Duration", true).render(IMPORTS)).isEqualTo("time.Duration");
        assertThat(new GoType.StreamResult(string).render(IMPORTS)).isEqualTo("iter.Seq2[string, error]");
    }

    @Test
    void shouldRenderAFunctionWithAndWithoutAResult() {
        final GoType string = new GoType.Predeclared("string");
        assertThat(new GoType.Func(List.of(string, new GoType.Predeclared("int32")), new GoType.Void())
                .render(IMPORTS)).isEqualTo("func(string, int32)");
        assertThat(new GoType.Func(List.of(string), string).render(IMPORTS)).isEqualTo("func(string) string");
        assertThat(new GoType.Func(List.of(), new GoType.Void()).render(IMPORTS)).isEqualTo("func()");
    }

    @Test
    void shouldRenderADeclaredTypeWithItsArguments() {
        assertThat(new GoType.Declared("example.com/m/a", "Page", false, true,
                List.of(new GoType.Predeclared("string"))).render(IMPORTS)).isEqualTo("Page[string]");
        assertThat(new GoType.Declared("example.com/m/b", "Box", false, true, List.of())
                .render(IMPORTS)).isEqualTo("b.Box");
    }
}
