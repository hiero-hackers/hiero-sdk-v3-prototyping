package org.hiero.sdk.v3.metalang.generator.java;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;

/**
 * What the generators of single types need to know about the whole model: which types are generated (only those can
 * be used in {@code implements}/{@code extends} and {@code @Override}), the Java module of every namespace, and the
 * original declaration of inherited members (an implementation must keep the Java types of the member it
 * implements).
 */
final class JavaContext {

    private final LinkedModel model;
    private final Set<QualifiedName> generated;
    private final Map<String, String> moduleOfNamespace;
    private final Set<QualifiedName> classes;
    private final Map<String, QualifiedName> exceptions;
    private final Set<String> typeNames;

    /**
     * Creates a context.
     *
     * @param model             the linked model
     * @param generated         the types that are generated
     * @param moduleOfNamespace the Java module of every namespace
     * @param classes           the types that are (abstract or concrete) Java classes
     * @param exceptions        the generated exception class of every error identifier without standard exception
     */
    JavaContext(final LinkedModel model, final Set<QualifiedName> generated,
                final Map<String, String> moduleOfNamespace, final Set<QualifiedName> classes,
                final Map<String, QualifiedName> exceptions) {
        this.model = Objects.requireNonNull(model, "model must not be null");
        this.generated = Set.copyOf(generated);
        this.moduleOfNamespace = Map.copyOf(moduleOfNamespace);
        this.classes = Set.copyOf(classes);
        this.exceptions = Map.copyOf(exceptions);
        this.typeNames = model.types().stream().map(t -> t.name().name()).collect(Collectors.toUnmodifiableSet());
    }

    LinkedModel model() {
        return model;
    }

    /**
     * Returns the Java exception of an error identifier: the standard JDK exception or the generated class.
     *
     * @param errorId the error identifier of {@code @@throws}
     * @return the exception
     */
    JavaExceptions.JavaException exception(final String errorId) {
        return JavaExceptions.standard(errorId).orElseGet(() -> {
            final QualifiedName name = Objects.requireNonNull(exceptions.get(errorId),
                    () -> "no exception class for " + errorId);
            return new JavaExceptions.JavaException(JavaNames.packageName(name.namespace()), name.name(), false);
        });
    }

    /**
     * Returns the generated exception classes by error identifier.
     *
     * @return the exception classes
     */
    Map<String, QualifiedName> exceptions() {
        return exceptions;
    }

        boolean isGenerated(final QualifiedName name) {
        return generated.contains(name);
    }

    /**
     * Whether a type is a Java class (abstract or concrete); abstractions that are no class are interfaces.
     *
     * @param name the type
     * @return {@code true} for classes
     */
    boolean isClass(final QualifiedName name) {
        return classes.contains(name);
    }

    /**
     * Returns the supertype that is a Java class (at most one, the planning guarantees it).
     *
     * @param type the type
     * @return the superclass, if any
     */
    Optional<Type.DeclaredType> superclass(final TypeDefinition type) {
        return declaredSupertypes(type.name()).stream().filter(s -> classes.contains(s.name())).findFirst();
    }

    /**
     * Returns the Java module of a namespace, if it belongs to one.
     *
     * @param namespace the namespace
     * @return the module name
     */
    Optional<String> module(final String namespace) {
        return Optional.ofNullable(moduleOfNamespace.get(namespace));
    }

    /**
     * Creates the imports of the file of a type.
     *
     * @param type the generated type
     * @return the imports
     */
    Imports imports(final TypeDefinition type) {
        return imports(JavaNames.packageName(type.name().namespace()), type.name().name());
    }

    /**
     * Creates the imports of a file that declares a class which is no spec type (constants, factory).
     *
     * @param packageName the package of the file
     * @param className   the simple name of the declared class
     * @return the imports
     */
    Imports imports(final String packageName, final String className) {
        return new Imports(packageName, typeNames, className);
    }

    /**
     * Whether the Java type of {@code type} really extends or implements {@code supertype}: there is a path of
     * generated supertypes from one to the other.
     *
     * @param type      the type
     * @param supertype the potential supertype
     * @return {@code true} if Java sees the subtype relation
     */
    boolean inheritsFrom(final QualifiedName type, final QualifiedName supertype) {
        return inheritsFrom(type, supertype, new HashSet<>());
    }

    private boolean inheritsFrom(final QualifiedName type, final QualifiedName supertype,
                                 final Set<QualifiedName> visited) {
        if (!visited.add(type)) {
            return false;
        }
        for (final Type.DeclaredType direct : declaredSupertypes(type)) {
            if (generated.contains(direct.name())
                    && (direct.name().equals(supertype) || inheritsFrom(direct.name(), supertype, visited))) {
                return true;
            }
        }
        return false;
    }

    private List<Type.DeclaredType> declaredSupertypes(final QualifiedName type) {
        return model.type(type).stream().flatMap(d -> d.supertypes().stream())
                .filter(Type.DeclaredType.class::isInstance).map(Type.DeclaredType.class::cast).toList();
    }

    /**
     * Whether an attribute of {@code owner} implements an accessor of a generated supertype.
     *
     * @param owner the type that declares or inherits the attribute
     * @param name  the attribute name
     * @return {@code true} if the accessor needs {@code @Override}
     */
    boolean overridesAccessor(final QualifiedName owner, final String name) {
        return declaringSupertypes(owner, name).stream().anyMatch(d -> inheritsFrom(owner, d));
    }

    private List<QualifiedName> declaringSupertypes(final QualifiedName owner, final String name) {
        final List<QualifiedName> result = new ArrayList<>();
        for (final Type.DeclaredType supertype : declaredSupertypes(owner)) {
            final Optional<TypeDefinition> definition = model.type(supertype.name());
            if (definition.isPresent() && definition.get().field(name).isPresent()) {
                result.add(supertype.name());
            }
        }
        return result;
    }

    /**
     * Whether the accessor of an attribute must use the wrapper class of a primitive: if the attribute is, or
     * overrides or implements, an attribute that is declared with a type variable or as {@code @@nullable}. Java
     * accessors that implement each other must have the same Java type ({@code Long}, not {@code long}).
     *
     * @param owner the type whose accessor is generated
     * @param name  the attribute name
     * @return {@code true} if the wrapper class must be used
     */
    boolean boxed(final QualifiedName owner, final String name) {
        return boxed(owner, name, new HashSet<>());
    }

    private boolean boxed(final QualifiedName type, final String name, final Set<QualifiedName> visited) {
        if (!visited.add(type)) {
            return false;
        }
        final Optional<FieldDefinition> declared = model.type(type).flatMap(d -> declaredField(d, name));
        if (declared.isPresent() && (declared.get().type() instanceof Type.TypeVariable
                || declared.get().hasAnnotation("nullable"))) {
            return true;
        }
        return declaringSupertypes(type, name).stream().anyMatch(s -> boxed(s, name, visited));
    }

    private static Optional<FieldDefinition> declaredField(final TypeDefinition type, final String name) {
        final List<FieldDefinition> declared = type instanceof TypeDefinition.ComplexTypeDefinition complex
                ? complex.declaredFields() : type.fields();
        return declared.stream().filter(f -> f.name().equals(name)).findFirst();
    }

    /**
     * Returns the declaration of a method as written in its declaring type (inherited methods appear with the type
     * arguments of the supertype substituted).
     *
     * @param method the (possibly inherited) method
     * @return the original declaration, or the method itself
     */
    MethodDefinition origin(final MethodDefinition method) {
        if (method.declaringType() == null) {
            return method;
        }
        return model.type(method.declaringType()).stream()
                .flatMap(d -> d.declaredMethods().stream())
                .filter(m -> m.location().equals(method.location()))
                .findFirst()
                .orElse(method);
    }

    /**
     * Renders the supertypes of a type: generated supertypes with {@code keyword} ({@code implements} or
     * {@code extends}), the other ones as a comment.
     *
     * @param type    the type
     * @param keyword the Java keyword
     * @param imports the imports of the file
     * @return the clause, starting with a space, or an empty string
     */
    String supertypes(final TypeDefinition type, final String keyword, final Imports imports) {
        final List<String> real = new ArrayList<>();
        final List<String> pending = new ArrayList<>();
        for (final Type supertype : type.supertypes()) {
            if (supertype instanceof Type.DeclaredType declared && classes.contains(declared.name())) {
                continue; // the superclass is rendered with extends by the class generator
            }
            if (supertype instanceof Type.DeclaredType declared && generated.contains(declared.name())) {
                real.add(JavaTypes.type(declared, true, imports));
            } else {
                pending.add(supertype instanceof Type.DeclaredType d
                        ? JavaNames.packageName(d.name().namespace()) + "." + d.name().name() : supertype.text());
            }
        }
        final StringBuilder java = new StringBuilder();
        if (!real.isEmpty()) {
            java.append(' ').append(keyword).append(' ').append(String.join(", ", real));
        }
        if (!pending.isEmpty()) {
            java.append(" /* ").append(keyword).append(' ').append(String.join(", ", pending))
                    .append(" (enabled as soon as ").append(pending.size() == 1 ? "it is" : "they are")
                    .append(" generated) */");
        }
        return java.toString();
    }
}
