package org.hiero.sdk.v3.metalang.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.FunctionDefinition;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Checks the default instances ({@code ## Default Instances}): reports what the linker could not resolve
 * ({@link Rule#INSTANCE_INVALID}, {@link Rule#INSTANCE_CYCLE}) and warns about every type that attributes or
 * parameters use but that cannot be obtained through the API ({@link Rule#INSTANCE_MISSING}) — the generated tests
 * cannot pass such a value. A type can be obtained if it has a default instance, is an enum with a value, is a
 * complex type whose required attributes can be obtained, has a concrete subtype that can be obtained, or is returned
 * by a static method or a namespace-level function whose required parameters can be obtained.
 */
final class InstanceCheck implements Check {

    @Override
    public void run(final ValidationContext context, final DiagnosticCollector out) {
        final LinkedModel linked = context.linked();
        out.addAll(linked.instanceDiagnostics());
        final Set<QualifiedName> obtainable = obtainable(linked);
        final Map<QualifiedName, List<String>> users = new TreeMap<>();
        for (final TypeDefinition type : linked.types()) {
            if (type instanceof TypeDefinition.ComplexTypeDefinition complex) {
                for (final FieldDefinition field : complex.declaredFields()) {
                    use(field.type(), type.name() + "." + field.name(), users);
                }
            }
            for (final MethodDefinition method : type.declaredMethods()) {
                method.parameters().forEach(p -> use(p.type(), type.name() + "." + method.name() + "(" + p.name()
                        + ")", users));
            }
        }
        for (final FunctionDefinition function : linked.functions()) {
            function.method().parameters().forEach(p -> use(p.type(), function.namespace() + "."
                    + function.method().name() + "(" + p.name() + ")", users));
        }
        users.forEach((name, uses) -> {
            if (!obtainable.contains(name)) {
                final TypeDefinition type = linked.type(name).orElseThrow();
                out.report(Rule.INSTANCE_MISSING, "There is no way to obtain an instance of '" + name.name() + "' through the API "
                        + "(no default instance, no concrete subtype, no factory): the tests that need one cannot be "
                        + "generated (" + uses.size() + " use(s), e.g. " + uses.getFirst() + "); define its default "
                        + "instance in '## Default Instances'", location(type));
            }
        });
    }

    private static void use(final Type type, final String user, final Map<QualifiedName, List<String>> users) {
        if (type instanceof Type.DeclaredType declared) {
            users.computeIfAbsent(declared.name(), k -> new ArrayList<>()).add(user);
        }
    }

    private static SourceLocation location(final TypeDefinition type) {
        return switch (type) {
            case TypeDefinition.ComplexTypeDefinition complex -> complex.location();
            case TypeDefinition.EnumDefinition enumType -> enumType.location();
        };
    }

    /** The types that can be obtained, as greatest set reachable from the base cases (fixed point). */
    private static Set<QualifiedName> obtainable(final LinkedModel linked) {
        final Set<QualifiedName> result = new TreeSet<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (final TypeDefinition type : linked.types()) {
                if (!result.contains(type.name()) && isObtainable(type, linked, result)) {
                    result.add(type.name());
                    changed = true;
                }
            }
        }
        return result;
    }

    private static boolean isObtainable(final TypeDefinition type, final LinkedModel linked,
                                        final Set<QualifiedName> obtainable) {
        if (linked.instance(type.name()).isPresent()) {
            return true;
        }
        final boolean direct = switch (type) {
            case TypeDefinition.EnumDefinition enumType -> !enumType.values().isEmpty();
            case TypeDefinition.ComplexTypeDefinition complex when complex.abstraction() ->
                    linked.types().stream().anyMatch(t -> !t.name().equals(type.name())
                            && obtainable.contains(t.name()) && !isAbstraction(t)
                            && linked.isSubtypeOf(t.name(), type.name()));
            case TypeDefinition.ComplexTypeDefinition complex -> complex.fields().stream()
                    .allMatch(f -> f.hasAnnotation("nullable") || f.hasAnnotation("default")
                            || isObtainable(f.type(), obtainable));
        };
        if (direct) {
            return true;
        }
        final List<MethodDefinition> factories = new ArrayList<>(type.methods().stream()
                .filter(MethodDefinition::isStatic).toList());
        linked.functions().forEach(f -> factories.add(f.method()));
        return factories.stream().anyMatch(m -> returns(m, type.name()) && m.parameters().stream()
                .allMatch(p -> optional(p) || isObtainable(p.type(), obtainable)));
    }

    private static boolean returns(final MethodDefinition method, final QualifiedName type) {
        return method.returnType() instanceof Type.DeclaredType declared && declared.name().equals(type)
                && !method.hasAnnotation("nullable") && !method.hasAnnotation("async")
                && !method.hasAnnotation("streaming");
    }

    private static boolean optional(final ParameterDefinition parameter) {
        return parameter.hasAnnotation("nullable") || parameter.varargs();
    }

    private static boolean isAbstraction(final TypeDefinition type) {
        return type instanceof TypeDefinition.ComplexTypeDefinition complex && complex.abstraction();
    }

    private static boolean isObtainable(final Type type, final Set<QualifiedName> obtainable) {
        return switch (type) {
            case Type.DeclaredType declared -> obtainable.contains(declared.name());
            case Type.BasicType basic -> basic.builtin().category() != BuiltinType.Category.STREAM_RESULT;
            default -> true;
        };
    }
}
