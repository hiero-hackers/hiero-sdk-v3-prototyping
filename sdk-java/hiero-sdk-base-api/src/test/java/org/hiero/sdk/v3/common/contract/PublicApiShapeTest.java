package org.hiero.sdk.v3.common.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import org.hiero.sdk.v3.common.MirrorNodeException;
import org.hiero.sdk.v3.common.Page;
import org.junit.jupiter.api.Test;

class PublicApiShapeTest {
    @Test
    void pageHasTheRequiredTypeAndConstructorShape() throws ReflectiveOperationException {
        assertThat(Page.class.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isAbstract);

        final var constructor = Page.class.getDeclaredConstructor(List.class, int.class, int.class);
        assertThat(constructor.getModifiers()).matches(Modifier::isProtected);

        assertThat(Arrays.stream(Page.class.getDeclaredFields()))
                .allSatisfy(field -> assertThat(field.getModifiers())
                        .matches(Modifier::isPrivate)
                        .matches(Modifier::isFinal));
    }

    @Test
    void pageAccessorsAndNavigationHaveTheRequiredShape() throws ReflectiveOperationException {
        assertFinalMethod("data", List.class);
        assertFinalMethod("size", int.class);
        assertFinalMethod("pageIndex", int.class);
        assertAbstractMethod("hasNext", boolean.class);
        assertAbstractMethod("isFirst", boolean.class);

        final var next = assertAbstractMethod("next", CompletionStage.class);
        final var first = assertAbstractMethod("first", CompletionStage.class);
        final var expectedType = "java.util.concurrent.CompletionStage<org.hiero.sdk.v3.common.Page<T>>";
        assertThat(next.getGenericReturnType().getTypeName()).isEqualTo(expectedType);
        assertThat(first.getGenericReturnType().getTypeName()).isEqualTo(expectedType);
    }

    @Test
    void mirrorNodeExceptionHasTheRequiredShape() throws ReflectiveOperationException {
        assertThat(MirrorNodeException.class.getModifiers()).matches(Modifier::isPublic);
        assertThat(MirrorNodeException.class.getSuperclass()).isEqualTo(RuntimeException.class);
        assertThat(MirrorNodeException.class.getConstructor(String.class).getModifiers()).matches(Modifier::isPublic);
        assertThat(MirrorNodeException.class.getConstructor(String.class, Throwable.class).getModifiers())
                .matches(Modifier::isPublic);
    }

    private static void assertFinalMethod(final String name, final Class<?> returnType)
            throws ReflectiveOperationException {
        final var method = Page.class.getDeclaredMethod(name);
        assertThat(method.getReturnType()).isEqualTo(returnType);
        assertThat(method.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isFinal);
    }

    private static java.lang.reflect.Method assertAbstractMethod(final String name, final Class<?> returnType)
            throws ReflectiveOperationException {
        final var method = Page.class.getDeclaredMethod(name);
        assertThat(method.getReturnType()).isEqualTo(returnType);
        assertThat(method.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isAbstract);
        return method;
    }
}
