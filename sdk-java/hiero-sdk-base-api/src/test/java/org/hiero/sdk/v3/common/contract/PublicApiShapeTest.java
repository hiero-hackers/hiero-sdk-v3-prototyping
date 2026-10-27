package org.hiero.sdk.v3.common.contract;

import java.lang.module.ModuleDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import org.hiero.sdk.v3.common.MirrorNodeException;
import org.hiero.sdk.v3.common.Page;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PublicApiShapeTest {

    @Test
    void pageHasTheRequiredTypeAndConstructorShape() throws ReflectiveOperationException {
        // GIVEN
        final Class<?> type = Page.class;
        final Constructor<?> constructor = type.getDeclaredConstructor(List.class, int.class, int.class);
        // WHEN / THEN
        assertThat(type.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isAbstract);
        assertThat(type.getDeclaredConstructors()).hasSize(1);
        assertThat(constructor.getModifiers()).matches(Modifier::isProtected);
        assertThat(Arrays.stream(type.getDeclaredFields()).map(field -> field.getName()).toList())
                .containsExactlyInAnyOrder("data", "size", "pageIndex");
        assertThat(Arrays.stream(type.getDeclaredFields()).toList())
                .allSatisfy(field -> assertThat(field.getModifiers())
                        .matches(Modifier::isPrivate).matches(Modifier::isFinal));
    }

    @Test
    void pageAccessorsAndNavigationHaveTheRequiredShape() throws ReflectiveOperationException {
        // GIVEN / WHEN / THEN
        assertFinalMethod("data", List.class);
        assertFinalMethod("size", int.class);
        assertFinalMethod("pageIndex", int.class);
        assertAbstractMethod("hasNext", boolean.class);
        assertAbstractMethod("isFirst", boolean.class);
        final Method next = assertAbstractMethod("next", CompletionStage.class);
        final Method first = assertAbstractMethod("first", CompletionStage.class);
        final String expectedType = "java.util.concurrent.CompletionStage<org.hiero.sdk.v3.common.Page<T>>";
        assertThat(next.getGenericReturnType().getTypeName()).isEqualTo(expectedType);
        assertThat(first.getGenericReturnType().getTypeName()).isEqualTo(expectedType);
        assertThat(Arrays.stream(Page.class.getDeclaredMethods()).map(Method::getName).toList())
                .containsExactlyInAnyOrder("data", "size", "pageIndex", "hasNext", "isFirst", "next", "first");
    }

    @Test
    void publicReferenceTypesHaveExplicitNullability() throws ReflectiveOperationException {
        // GIVEN
        final Constructor<?> constructor = Page.class.getDeclaredConstructor(List.class, int.class, int.class);
        // WHEN / THEN
        assertThat(Page.class.getPackage().isAnnotationPresent(NullMarked.class)).isTrue();
        assertThat(constructor.getAnnotatedParameterTypes()[0].isAnnotationPresent(NonNull.class)).isTrue();
        for (final String method : List.of("data", "next", "first")) {
            assertThat(Page.class.getDeclaredMethod(method).getAnnotatedReturnType()
                    .isAnnotationPresent(NonNull.class)).as(method).isTrue();
        }
        for (final Constructor<?> exceptionConstructor : MirrorNodeException.class.getConstructors()) {
            for (final java.lang.reflect.AnnotatedType parameter : exceptionConstructor.getAnnotatedParameterTypes()) {
                assertThat(parameter.isAnnotationPresent(Nullable.class)).isTrue();
            }
        }
    }

    @Test
    void mirrorNodeExceptionHasTheRequiredShape() throws ReflectiveOperationException {
        // GIVEN / WHEN / THEN
        assertThat(MirrorNodeException.class.getModifiers()).matches(Modifier::isPublic);
        assertThat(MirrorNodeException.class.getSuperclass()).isEqualTo(RuntimeException.class);
        assertThat(MirrorNodeException.class.getConstructor(String.class).getModifiers()).matches(Modifier::isPublic);
        assertThat(MirrorNodeException.class.getConstructor(String.class, Throwable.class).getModifiers())
                .matches(Modifier::isPublic);
    }

    @Test
    void productionModuleExportsOnlyThePublicApi() {
        // GIVEN
        final Module module = Page.class.getModule();
        final ModuleDescriptor descriptor = module.getDescriptor();
        // WHEN / THEN
        assertThat(module.isNamed()).isTrue();
        assertThat(descriptor.name()).isEqualTo("org.hiero.sdk.v3.base");
        assertThat(descriptor.isOpen()).isFalse();
        assertThat(descriptor.opens()).isEmpty();
        assertThat(descriptor.exports()).extracting(ModuleDescriptor.Exports::source)
                .containsExactlyInAnyOrder("org.hiero.sdk.v3.common", "org.hiero.sdk.v3.ledger",
                        "org.hiero.sdk.v3.nativeToken");
        assertThat(descriptor.requires()).extracting(ModuleDescriptor.Requires::name)
                .containsExactlyInAnyOrder("java.base", "org.jspecify");
        final ModuleDescriptor.Requires annotations = descriptor.requires().stream()
                .filter(requirement -> requirement.name().equals("org.jspecify")).findFirst().orElseThrow();
        assertThat(annotations.modifiers()).containsExactly(ModuleDescriptor.Requires.Modifier.STATIC);
    }

    @Test
    void publicSignaturesDoNotExposeImplementationOrTestTypes() {
        // GIVEN / WHEN / THEN
        for (final Method method : Page.class.getDeclaredMethods()) {
            assertThat(method.getReturnType().isPrimitive()
                    || method.getReturnType().getPackageName().startsWith("java.")).as(method.getName()).isTrue();
            assertThat(method.getParameterTypes()).isEmpty();
            assertThat(method.getExceptionTypes()).isEmpty();
        }
    }

    private static void assertFinalMethod(final String name, final Class<?> returnType)
            throws ReflectiveOperationException {
        final Method method = Page.class.getDeclaredMethod(name);
        assertThat(method.getReturnType()).isEqualTo(returnType);
        assertThat(method.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isFinal);
    }

    private static Method assertAbstractMethod(final String name, final Class<?> returnType)
            throws ReflectiveOperationException {
        final Method method = Page.class.getDeclaredMethod(name);
        assertThat(method.getReturnType()).isEqualTo(returnType);
        assertThat(method.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isAbstract);
        return method;
    }
}
