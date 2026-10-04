package org.hiero.sdk.v3.metalang.generator.ts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.hiero.sdk.v3.metalang.generator.GenerationException;
import org.hiero.sdk.v3.metalang.generator.SpecFolders;
import org.hiero.sdk.v3.metalang.model.ConstantDefinition;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.FunctionDefinition;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * What the TypeScript generator generates and where: the packages (spec folders), the declarations that can be
 * generated (a declaration that refers to a type without TypeScript mapping is deferred, together with everything
 * that refers to it) and the home of every error class.
 */
final class TsContext {

    /** Error identifiers that map to a standard JavaScript error. */
    private static final Map<String, String> STANDARD_ERRORS = Map.of(
            "illegal-format", "RangeError",
            "invalid-argument-error", "RangeError");

    /** Global names that type variables must not hide. */
    private static final Set<String> GLOBALS = Set.of("Array", "Boolean", "Date", "Error", "Function", "Map",
            "Number", "Object", "Promise", "RangeError", "Set", "String", "Symbol", "TypeError", "Uint8Array",
            "Duration", "StreamItem", "AbstractConstructor", "AsyncIterable", "ReadonlyArray", "ReadonlyMap",
            "ReadonlySet", "Record", "Partial", "Readonly");

    /**
     * An error class: generated in the namespace, or a standard error ({@code namespace} is {@code null}).
     *
     * @param namespace the namespace of the generated class, {@code null} for a standard error
     * @param name      the class name
     */
    record ErrorClass(String namespace, String name) {
    }

    private final TsGeneratorConfig config;
    private final LinkedModel model;
    private final List<SpecFolders.Folder> folders;
    private final Map<String, String> folderOf = new HashMap<>();
    private final Set<QualifiedName> generated;
    private final SortedMap<QualifiedName, String> deferred = new TreeMap<>();
    private final Map<String, List<FunctionDefinition>> functions = new TreeMap<>();
    private final Map<String, List<ConstantDefinition>> constants = new TreeMap<>();
    private final Map<String, ErrorClass> errors = new TreeMap<>();
    private final Set<String> typeNames;

    TsContext(final TsGeneratorConfig config, final LinkedModel model) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.model = Objects.requireNonNull(model, "model must not be null");
        this.folders = SpecFolders.of(model, "npm package");
        folders.forEach(f -> f.namespaces().forEach(n -> folderOf.put(n.name(), f.name())));
        this.typeNames = new HashSet<>();
        model.types().forEach(t -> typeNames.add(t.name().name()));
        this.generated = plan();
        planFunctionsAndConstants();
        planErrors();
    }

    TsGeneratorConfig config() {
        return config;
    }

    LinkedModel model() {
        return model;
    }

    List<SpecFolders.Folder> folders() {
        return folders;
    }

    String folder(final String namespace) {
        return Objects.requireNonNull(folderOf.get(namespace), () -> "no spec folder for namespace " + namespace);
    }

    boolean isGenerated(final QualifiedName type) {
        return generated.contains(type);
    }

    SortedMap<QualifiedName, String> deferred() {
        return deferred;
    }

    List<FunctionDefinition> functions(final String namespace) {
        return functions.getOrDefault(namespace, List.of());
    }

    List<FunctionDefinition> functions() {
        return functions.values().stream().flatMap(List::stream).toList();
    }

    List<ConstantDefinition> constants(final String namespace) {
        return constants.getOrDefault(namespace, List.of());
    }

    Map<String, ErrorClass> errors() {
        return errors;
    }

    ErrorClass error(final String errorId) {
        return Objects.requireNonNull(errors.get(errorId), () -> "no error class for " + errorId);
    }

    /** Whether a type is a class: a complex type that is no abstraction (abstractions are interfaces). */
    boolean isClass(final QualifiedName type) {
        return model.type(type).filter(TypeDefinition.ComplexTypeDefinition.class::isInstance)
                .map(t -> !((TypeDefinition.ComplexTypeDefinition) t).abstraction()).orElse(false);
    }

    boolean isEnum(final QualifiedName type) {
        return model.type(type).filter(TypeDefinition.EnumDefinition.class::isInstance).isPresent();
    }

    /** The superclass of a class: its generated concrete supertype. */
    Optional<Type.DeclaredType> superclass(final TypeDefinition type) {
        return type.supertypes().stream().filter(Type.DeclaredType.class::isInstance)
                .map(Type.DeclaredType.class::cast).filter(s -> isClass(s.name()) && generated.contains(s.name()))
                .findFirst();
    }

    /** Whether another type extends the type (then a class must not freeze its instances). */
    boolean hasSubtypes(final QualifiedName type) {
        return model.types().stream().anyMatch(t -> t.supertypes().stream()
                .anyMatch(s -> s instanceof Type.DeclaredType d && d.name().equals(type)));
    }

    /** The TypeScript name of a type variable: without {@code $$}, with a {@code T} if it would hide a type. */
    String typeVariable(final String name) {
        String ts = name.startsWith("$$") ? name.substring(2) : name;
        while (typeNames.contains(ts) || GLOBALS.contains(ts)) {
            ts = ts + "T";
        }
        return ts;
    }

    // --- planning --------------------------------------------------------------------------------

    /** The types that can be generated: a greatest fixed point over the types with a TypeScript mapping. */
    private Set<QualifiedName> plan() {
        final Set<QualifiedName> candidates = new TreeSet<>();
        for (final TypeDefinition type : model.types()) {
            if (folderOf.containsKey(type.name().namespace())) {
                candidates.add(type.name());
            }
        }
        boolean changed = true;
        while (changed) {
            changed = false;
            for (final QualifiedName name : List.copyOf(candidates)) {
                final Optional<String> reason = problem(referenced(model.type(name).orElseThrow()), candidates);
                if (reason.isPresent()) {
                    candidates.remove(name);
                    deferred.put(name, reason.get());
                    changed = true;
                }
            }
        }
        return candidates;
    }

    private void planFunctionsAndConstants() {
        for (final FunctionDefinition function : model.functions()) {
            if (!folderOf.containsKey(function.namespace())) {
                continue;
            }
            final List<Type> types = new ArrayList<>();
            methodTypes(function.method(), types);
            final Optional<String> reason = problem(types, generated);
            if (reason.isPresent()) {
                deferred.put(new QualifiedName(function.namespace(), function.method().signature()), reason.get());
            } else {
                functions.computeIfAbsent(function.namespace(), k -> new ArrayList<>()).add(function);
            }
        }
        for (final ConstantDefinition constant : model.constants()) {
            if (!folderOf.containsKey(constant.name().namespace())) {
                continue;
            }
            final Optional<String> reason = problem(List.of(constant.type()), generated);
            if (reason.isPresent()) {
                deferred.put(constant.name(), reason.get());
            } else {
                constants.computeIfAbsent(constant.name().namespace(), k -> new ArrayList<>()).add(constant);
            }
        }
    }

    /** Places every error class in a namespace that uses it and whose package all using packages require. */
    private void planErrors() {
        final Map<String, Set<String>> usage = new TreeMap<>();
        for (final TypeDefinition type : model.types()) {
            if (generated.contains(type.name())) {
                type.declaredMethods().forEach(m -> errorIds(m).forEach(id -> usage.computeIfAbsent(id,
                        k -> new TreeSet<>()).add(type.name().namespace())));
            }
        }
        functions().forEach(f -> errorIds(f.method()).forEach(id -> usage.computeIfAbsent(id, k -> new TreeSet<>())
                .add(f.namespace())));
        final List<String> problems = new ArrayList<>();
        usage.forEach((errorId, namespaces) -> {
            if (STANDARD_ERRORS.containsKey(errorId)) {
                errors.put(errorId, new ErrorClass(null, STANDARD_ERRORS.get(errorId)));
                return;
            }
            final Set<String> users = new TreeSet<>();
            namespaces.forEach(n -> users.add(folder(n)));
            final Optional<String> home = namespaces.stream()
                    .filter(n -> users.stream().allMatch(u -> u.equals(folder(n))
                            || SpecFolders.required(u, folders).contains(folder(n))))
                    .min(Comparator.comparing(String::length).thenComparing(n -> n));
            if (home.isEmpty()) {
                problems.add("Error '" + errorId + "' is used in the packages " + users + ", but none of them is "
                        + "required by all others; its error class has no home");
            } else {
                errors.put(errorId, new ErrorClass(home.get(), TsNames.errorClass(errorId)));
            }
        });
        if (!problems.isEmpty()) {
            throw new GenerationException(problems);
        }
    }

    // --- references ------------------------------------------------------------------------------

    private static List<String> errorIds(final MethodDefinition method) {
        return method.annotation("throws").stream().flatMap(t -> t.arguments().stream()).map(a -> a.text())
                .distinct().toList();
    }

    /** Every type a type declaration refers to. */
    static List<Type> referenced(final TypeDefinition type) {
        final List<Type> types = new ArrayList<>(type.supertypes());
        type.typeParameters().stream().filter(p -> p.bound() != null).forEach(p -> types.add(p.bound()));
        type.fields().stream().map(FieldDefinition::type).forEach(types::add);
        type.methods().forEach(m -> methodTypes(m, types));
        return types;
    }

    private static void methodTypes(final MethodDefinition method, final List<Type> out) {
        method.typeParameters().stream().filter(p -> p.bound() != null).forEach(p -> out.add(p.bound()));
        out.add(method.returnType());
        method.parameters().forEach(p -> out.add(p.type()));
    }

    /** Why a declaration with these types cannot be generated, if it cannot. */
    private Optional<String> problem(final List<Type> types, final Set<QualifiedName> available) {
        for (final Type type : types) {
            final Optional<String> problem = problem(type, available);
            if (problem.isPresent()) {
                return problem;
            }
        }
        return Optional.empty();
    }

    private Optional<String> problem(final Type type, final Set<QualifiedName> available) {
        return switch (type) {
            case Type.UnresolvedType unresolved -> Optional.of("Type '" + unresolved.text()
                    + "' has no TypeScript mapping yet");
            case Type.DeclaredType declared -> available.contains(declared.name())
                    ? problem(declared.arguments(), available)
                    : Optional.of("refers to " + declared.name() + " (not generated yet)");
            case Type.BasicType basic -> basic.builtin().category() == BuiltinType.Category.INTEGER
                    && basic.builtin().bits() <= 0 ? Optional.of("invalid integer")
                    : problem(basic.arguments(), available);
            case Type.WildcardType wildcard -> wildcard.upperBound() == null ? Optional.empty()
                    : problem(wildcard.upperBound(), available);
            case Type.FunctionType function -> {
                final List<Type> types = new ArrayList<>();
                types.add(function.returnType());
                function.parameters().forEach(p -> types.add(p.type()));
                yield problem(types, available);
            }
            default -> Optional.empty();
        };
    }
}
