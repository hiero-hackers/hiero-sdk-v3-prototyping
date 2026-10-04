package org.hiero.sdk.v3.metalang.generator.rust;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.generator.GenerationException;
import org.hiero.sdk.v3.metalang.generator.SpecFolders;
import org.hiero.sdk.v3.metalang.model.ConstantDefinition;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.FunctionDefinition;
import org.hiero.sdk.v3.metalang.model.InstanceDefinition;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * What the Rust generator generates and how: the crates (spec folders), the declarations that can be generated, the
 * type parameters that are erased (a parameter used with {@code ANY} has no Rust counterpart: Rust has no wildcards,
 * so the parameter is replaced by its bound, as trait object), the {@code $$Self} parameters (Rust's {@code Self}),
 * the names of overloaded methods and functions (Rust has no overloading), the error types, and which traits the
 * generated types can derive.
 */
final class RustContext {

    /** Names of the prelude and of the standard items the generated code uses unqualified. */
    static final Set<String> PRELUDE = Set.of("String", "Vec", "Option", "Some", "None", "Result", "Ok", "Err",
            "Box", "Send", "Sync", "Sized", "Clone", "Copy", "PartialEq", "Eq", "Hash", "Default", "From", "Into",
            "Fn", "Iterator", "ToString", "Self", "Arc", "HashMap", "HashSet", "Any", "Debug", "Display",
            "PhantomData", "LazyLock", "FromStr", "Future", "Pin");

    /** Error identifiers that map to the support type {@code InvalidArgumentError}. */
    private static final Set<String> STANDARD_ERRORS = Set.of("illegal-format", "invalid-argument-error");

    /**
     * An error type: generated in a namespace, or the support type ({@code namespace} is {@code null}).
     *
     * @param namespace the namespace of the generated type, {@code null} for {@code InvalidArgumentError}
     * @param name      the type name
     */
    record ErrorType(String namespace, String name) {
    }

    /**
     * The error enum of a method or function that declares several errors.
     *
     * @param namespace the namespace it is declared in
     * @param name      the enum name
     * @param errors    the error identifiers with their types, in declaration order
     */
    record ErrorEnum(String namespace, String name, Map<String, ErrorType> errors) {
    }

    /** Which standard traits a type implements (derives). */
    record Capabilities(boolean copy, boolean eq, boolean fullEq, boolean hash, boolean cloneable) {

        static final Capabilities ALL = new Capabilities(true, true, true, true, true);
        /** a shared value ({@code Arc}): only {@code Clone} */
        static final Capabilities SHARED = new Capabilities(false, false, false, false, true);

        Capabilities(final boolean copy, final boolean eq, final boolean fullEq, final boolean hash) {
            this(copy, eq, fullEq, hash, true);
        }

        Capabilities and(final Capabilities other) {
            return new Capabilities(copy && other.copy, eq && other.eq, fullEq && other.fullEq, hash && other.hash,
                    cloneable && other.cloneable);
        }
    }

    private final RustGeneratorConfig config;
    private final LinkedModel model;
    private final List<SpecFolders.Folder> folders;
    private final Map<String, String> folderOf = new HashMap<>();
    private final Set<String> typeNames = new HashSet<>();
    private final Map<QualifiedName, Set<Integer>> erased = new HashMap<>();
    private final Map<QualifiedName, Integer> selfParameter = new HashMap<>();
    private final Set<QualifiedName> generated;
    private final SortedMap<QualifiedName, String> deferred = new TreeMap<>();
    private final Map<String, List<FunctionDefinition>> functions = new TreeMap<>();
    private final Map<String, List<ConstantDefinition>> constants = new TreeMap<>();
    private final Map<String, ErrorType> errors = new TreeMap<>();
    private final Map<SourceLocation, String> methodNames = new HashMap<>();
    private final Map<SourceLocation, ErrorEnum> errorEnums = new HashMap<>();
    private final Map<QualifiedName, Capabilities> capabilities = new HashMap<>();
    private final Set<QualifiedName> capabilitiesInProgress = new HashSet<>();
    private final String supportFolder;

    RustContext(final RustGeneratorConfig config, final LinkedModel model) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.model = Objects.requireNonNull(model, "model must not be null");
        this.folders = SpecFolders.of(model, "crate");
        folders.forEach(f -> f.namespaces().forEach(n -> folderOf.put(n.name(), f.name())));
        model.types().forEach(t -> typeNames.add(t.name().name()));
        planParameters();
        this.generated = plan();
        planFunctionsAndConstants();
        planNames();
        planErrors();
        this.supportFolder = folders.isEmpty() ? null : SpecFolders.common(new HashSet<>(folderOf.values()), folders)
                .orElseThrow(() -> new GenerationException(List.of("The support module needs a crate that all "
                        + "crates require, but there is none among " + folderOf.values().stream().sorted().distinct()
                        .toList())));
    }

    RustGeneratorConfig config() {
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

    Map<String, ErrorType> errors() {
        return errors;
    }

    List<ErrorEnum> errorEnums(final String namespace) {
        return errorEnums.values().stream().filter(e -> e.namespace().equals(namespace)).distinct()
                .sorted(Comparator.comparing(ErrorEnum::name)).toList();
    }

    String supportFolder() {
        return Objects.requireNonNull(supportFolder, "no support crate");
    }

    // --- kinds of types ------------------------------------------------------------------------------

    boolean isAbstraction(final QualifiedName type) {
        return model.type(type).filter(TypeDefinition.ComplexTypeDefinition.class::isInstance)
                .map(t -> ((TypeDefinition.ComplexTypeDefinition) t).abstraction()).orElse(false);
    }

    boolean isSealed(final QualifiedName type) {
        return model.type(type).map(t -> t.hasAnnotation("sealed")).orElse(false) && isAbstraction(type);
    }

    /** Whether a type becomes a trait: an abstraction that is not sealed. */
    boolean isTrait(final QualifiedName type) {
        return isAbstraction(type) && !isSealed(type);
    }

    boolean isStruct(final QualifiedName type) {
        return model.type(type).filter(TypeDefinition.ComplexTypeDefinition.class::isInstance)
                .map(t -> !((TypeDefinition.ComplexTypeDefinition) t).abstraction()).orElse(false);
    }

    /** The permitted types of a sealed abstraction, in the order of the annotation. */
    List<QualifiedName> sealedVariants(final TypeDefinition type) {
        final List<QualifiedName> result = new ArrayList<>();
        type.annotation("sealed").ifPresent(a -> a.arguments().forEach(argument -> {
            final String name = argument.text();
            model.types().stream().filter(t -> t.name().name().equals(name) || t.name().toString().equals(name))
                    .filter(t -> model.isSubtypeOf(t.name(), type.name())).findFirst()
                    .ifPresent(t -> result.add(t.name()));
        }));
        return result;
    }

    // --- type parameters -----------------------------------------------------------------------------

    /** Whether a type parameter is erased: the specs use the type with {@code ANY} for it. */
    boolean isErased(final QualifiedName type, final int index) {
        return erased.getOrDefault(type, Set.of()).contains(index);
    }

    /** Whether a type parameter is the {@code $$Self} parameter of a trait. */
    boolean isSelf(final QualifiedName type, final int index) {
        final Integer self = selfParameter.get(type);
        return self != null && self == index;
    }

    /** The type parameters that remain type parameters in Rust. */
    List<TypeParameterDefinition> keptParameters(final TypeDefinition type) {
        final List<TypeParameterDefinition> result = new ArrayList<>();
        for (int i = 0; i < type.typeParameters().size(); i++) {
            if (!isErased(type.name(), i) && !isSelf(type.name(), i)) {
                result.add(type.typeParameters().get(i));
            }
        }
        return result;
    }

    /** The Rust name of a type parameter: without {@code $$}, with a {@code T} if it would hide a type. */
    String variableName(final String name) {
        String rust = name.substring(2); // type parameters start with $$
        while (typeNames.contains(rust) || PRELUDE.contains(rust)) {
            rust = rust + "T";
        }
        return rust;
    }

    /**
     * The scope of a type declaration: its type variables mapped to their Rust form (a type parameter,
     * {@code Self}, or the erased form).
     */
    Map<Type.TypeVariable, RustType> scope(final TypeDefinition type) {
        return scope(type, Map.of());
    }

    /**
     * The scope of a type as implemented by another type: its kept type parameters are the type arguments of the
     * implementing type.
     *
     * @param type      the implemented trait or extended struct
     * @param seen      the supertype as the implementing type declares it
     * @param outer     the scope of the implementing type
     * @return the scope
     */
    Map<Type.TypeVariable, RustType> implScope(final TypeDefinition type, final Type.DeclaredType seen,
                                               final Map<Type.TypeVariable, RustType> outer) {
        final Map<Integer, RustType> kept = new HashMap<>();
        for (int i = 0; i < type.typeParameters().size() && i < seen.arguments().size(); i++) {
            if (!isErased(type.name(), i) && !isSelf(type.name(), i)) {
                kept.put(i, rustType(seen.arguments().get(i), outer));
            }
        }
        return scope(type, kept);
    }

    private Map<Type.TypeVariable, RustType> scope(final TypeDefinition type, final Map<Integer, RustType> kept) {
        final Map<Type.TypeVariable, RustType> scope = new HashMap<>();
        final Map<Type.TypeVariable, RustType> inner = new HashMap<>();
        for (int i = 0; i < type.typeParameters().size(); i++) {
            final TypeParameterDefinition parameter = type.typeParameters().get(i);
            final RustType form;
            if (isSelf(type.name(), i)) {
                form = new RustType.SelfType();
            } else if (isErased(type.name(), i)) {
                inner.put(parameter.variable(), new RustType.AnyValue());
                continue;
            } else {
                form = kept.getOrDefault(i, new RustType.Parameter(variableName(parameter.name())));
            }
            scope.put(parameter.variable(), form);
            inner.put(parameter.variable(), form);
        }
        for (int i = 0; i < type.typeParameters().size(); i++) {
            final TypeParameterDefinition parameter = type.typeParameters().get(i);
            if (isErased(type.name(), i)) {
                // a bound that refers to erased parameters (Transaction<$$Receipt, $$Transaction>) cannot name them
                scope.put(parameter.variable(), parameter.bound() == null ? new RustType.AnyValue()
                        : rustType(parameter.bound(), inner));
            }
        }
        return scope;
    }

    /** The type arguments of a type that remain type arguments in Rust. */
    List<RustType> keptArguments(final Type.DeclaredType type, final Map<Type.TypeVariable, RustType> scope) {
        final List<RustType> result = new ArrayList<>();
        for (int i = 0; i < type.arguments().size(); i++) {
            if (!isErased(type.name(), i) && !isSelf(type.name(), i)) {
                result.add(rustType(type.arguments().get(i), scope));
            }
        }
        return result;
    }

    /** The scope of a method: the scope of its type with the type parameters of the method. */
    Map<Type.TypeVariable, RustType> scope(final Map<Type.TypeVariable, RustType> base, final MethodDefinition method) {
        final Map<Type.TypeVariable, RustType> scope = new HashMap<>(base);
        method.typeParameters().forEach(p -> scope.put(p.variable(), new RustType.Parameter(variableName(p.name()))));
        return scope;
    }

    /** The attributes a trait declares itself (not the narrowed ones of a supertrait). */
    List<FieldDefinition> traitFields(final TypeDefinition type) {
        final Set<String> inherited = new HashSet<>();
        for (final Type supertype : type.supertypes()) {
            if (supertype instanceof Type.DeclaredType declared) {
                model.type(declared.name()).ifPresent(d -> d.fields().forEach(f -> inherited.add(f.name())));
            }
        }
        return ((TypeDefinition.ComplexTypeDefinition) type).declaredFields().stream().filter(f -> !inherited.contains(f.name())).toList();
    }

    /**
     * The methods a trait declares itself: not the static ones (associated functions of the trait object), not the
     * final ones (extension trait), not {@code toString()} ({@code Display}) and not the overriding ones.
     */
    List<MethodDefinition> traitMethods(final TypeDefinition type) {
        return type.declaredMethods().stream().filter(m -> !m.isStatic() && !m.hasAnnotation("finalMethod")
                && !isDisplay(m) && overridden(type, m).isEmpty()).toList();
    }

    /** Whether constructing the struct can fail (an attribute is checked). */
    boolean isFallible(final QualifiedName type) {
        return model.type(type).map(t -> t.fields().stream().anyMatch(RustConstraints::isChecked)).orElse(false);
    }

    /** The module (file) name of a type: its snake-case name, unless a module of the namespace has this name. */
    String fileModule(final TypeDefinition type) {
        final String namespace = type.name().namespace();
        final Set<String> taken = new HashSet<>(Set.of("functions", "constants", "errors", "mod", RustNames.SUPPORT));
        folderOf.keySet().stream().filter(n -> n.startsWith(namespace + "."))
                .forEach(n -> taken.add(RustNames.snake(n.substring(namespace.length() + 1).split("\\.")[0])));
        final String name = RustNames.snake(type.name().name());
        // a module with the name of its parent module is confusing (clippy::module_inception)
        taken.add(RustNames.snake(namespace.substring(namespace.lastIndexOf('.') + 1)));
        return taken.contains(name) || !RustNames.identifier(name).equals(name) ? name + "_type" : name;
    }


    // --- Rust types ----------------------------------------------------------------------------------

    /**
     * Returns the Rust form of a type.
     *
     * @param type  the meta-language type
     * @param scope the type variables in scope with their Rust form
     * @return the Rust type
     */
    RustType rustType(final Type type, final Map<Type.TypeVariable, RustType> scope) {
        return switch (type) {
            case Type.BasicType basic -> basic(basic, scope);
            case Type.DeclaredType declared -> declared(declared, scope);
            case Type.TypeVariable variable -> scope.getOrDefault(variable,
                    new RustType.Parameter(variableName(variable.name())));
            case Type.WildcardType ignored -> new RustType.AnyValue();
            case Type.AnyType ignored -> new RustType.AnyValue();
            case Type.FunctionType function -> new RustType.Function(function.parameters().stream()
                    .map(p -> p.varargs() ? new RustType.VecOf(rustType(p.type(), scope)) : rustType(p.type(), scope))
                    .toList(), rustType(function.returnType(), scope));
            case Type.VoidType ignored -> new RustType.Unit();
            case Type.UnresolvedType unresolved -> throw new IllegalStateException("unresolved " + unresolved.text());
        };
    }

    private RustType basic(final Type.BasicType type, final Map<Type.TypeVariable, RustType> scope) {
        final BuiltinType builtin = type.builtin();
        final List<Type> arguments = type.arguments();
        return switch (builtin.category()) {
            case INTEGER -> new RustType.Primitive(integer(builtin), true, true, true);
            case FLOAT -> new RustType.Primitive("f64", true, true, false);
            case DECIMAL -> new RustType.Primitive("rust_decimal::Decimal", true, true, true);
            case BOOL -> new RustType.Primitive("bool", true, true, true);
            case STRING -> new RustType.Text();
            case BYTES -> new RustType.Bytes();
            case COLLECTION -> {
                final RustType element = rustType(arguments.getFirst(), scope);
                final Capabilities caps = capabilities(element);
                yield builtin.name().equals("set") && caps.fullEq() && caps.hash() ? new RustType.SetOf(element)
                        : new RustType.VecOf(element);
            }
            case MAP -> {
                final RustType key = rustType(arguments.get(0), scope);
                final RustType value = rustType(arguments.get(1), scope);
                final Capabilities caps = capabilities(key);
                yield caps.fullEq() && caps.hash() ? new RustType.MapOf(key, value) : new RustType.Pairs(key, value);
            }
            case TYPE -> new RustType.Primitive("std::any::TypeId", true, true, true);
            case UUID -> new RustType.Primitive("uuid::Uuid", true, true, true);
            case TEMPORAL -> new RustType.Primitive(switch (builtin.name()) {
                case "date" -> "chrono::NaiveDate";
                case "time" -> "chrono::NaiveTime";
                case "dateTime" -> "chrono::NaiveDateTime";
                default -> "chrono::DateTime<chrono::FixedOffset>";
            }, true, true, true);
            case DURATION -> new RustType.Primitive("std::time::Duration", true, true, true);
            case STREAM_RESULT -> new RustType.StreamItem(rustType(arguments.getFirst(), scope));
        };
    }

    /** The Rust integer type of a meta-language integer type: the smallest that holds all its values. */
    static String integer(final BuiltinType builtin) {
        final boolean unsigned = builtin.name().startsWith("u");
        final int bits = builtin.bits();
        final int width = bits <= 8 ? 8 : bits <= 16 ? 16 : bits <= 32 ? 32 : bits <= 64 ? 64 : bits <= 128 ? 128 : 256;
        if (width == 256) {
            return unsigned ? "ethnum::U256" : "ethnum::I256";
        }
        return (unsigned ? "u" : "i") + width;
    }

    /** Whether the Rust integer type holds more values than the meta-language type (a range check is needed). */
    static boolean needsRangeCheck(final BuiltinType builtin) {
        final int bits = builtin.bits();
        return bits != 8 && bits != 16 && bits != 32 && bits != 64 && bits != 128 && bits != 256;
    }

    private RustType declared(final Type.DeclaredType type, final Map<Type.TypeVariable, RustType> scope) {
        final TypeDefinition definition = model.definition(type);
        if (definition instanceof TypeDefinition.EnumDefinition) {
            return new RustType.Enum(type.name());
        }
        if (isSealed(type.name())) {
            return new RustType.Sealed(type.name());
        }
        final List<RustType> arguments = new ArrayList<>();
        for (int i = 0; i < type.arguments().size(); i++) {
            if (!isErased(type.name(), i) && !isSelf(type.name(), i)) {
                arguments.add(rustType(type.arguments().get(i), scope));
            }
        }
        return isAbstraction(type.name()) ? new RustType.Dyn(type.name(), arguments)
                : new RustType.Struct(type.name(), arguments);
    }

    /** The traits a Rust type implements: what a struct that contains it can derive. */
    Capabilities capabilities(final RustType type) {
        return switch (type) {
            case RustType.Primitive primitive -> new Capabilities(primitive.copy(), primitive.eq(),
                    primitive.eq() && primitive.hash(), primitive.hash());
            case RustType.Text ignored -> new Capabilities(false, true, true, true);
            case RustType.Bytes ignored -> new Capabilities(false, true, true, true);
            case RustType.VecOf vec -> new Capabilities(false, true, true, true).and(capabilities(vec.element()));
            case RustType.SetOf set -> new Capabilities(false, true, true, false).and(capabilities(set.element()));
            case RustType.MapOf map -> new Capabilities(false, true, true, false).and(capabilities(map.key()))
                    .and(capabilities(map.value()));
            case RustType.Pairs pairs -> new Capabilities(false, true, true, true).and(capabilities(pairs.key()))
                    .and(capabilities(pairs.value()));
            case RustType.Optional optional -> capabilities(optional.inner());
            case RustType.Struct struct -> new Capabilities(false, true, true, true)
                    .and(definitionCapabilities(struct.name())).and(struct.arguments().stream()
                            .map(this::capabilities).reduce(Capabilities.ALL, Capabilities::and));
            case RustType.Enum ignored -> Capabilities.ALL;
            case RustType.Sealed sealed -> new Capabilities(false, true, true, true)
                    .and(definitionCapabilities(sealed.name()));
            case RustType.Parameter ignored -> Capabilities.ALL;
            case RustType.Unit ignored -> Capabilities.ALL;
            case RustType.StreamItem ignored -> new Capabilities(false, false, false, false, false);
            default -> Capabilities.SHARED;
        };
    }

    /** The traits a generated struct or sealed enum derives (structs are never {@code Copy}). */
    Capabilities definitionCapabilities(final QualifiedName name) {
        final Capabilities cached = capabilities.get(name);
        if (cached != null) {
            return cached;
        }
        if (!capabilitiesInProgress.add(name)) {
            return Capabilities.ALL; // a cycle does not restrict
        }
        final TypeDefinition definition = model.type(name).orElseThrow();
        Capabilities result = new Capabilities(false, true, true, true);
        if (isSealed(name)) {
            for (final QualifiedName variant : sealedVariants(definition)) {
                result = result.and(capabilities(isAbstraction(variant) ? new RustType.Dyn(variant, List.of())
                        : new RustType.Struct(variant, List.of())));
            }
        } else {
            final Map<Type.TypeVariable, RustType> scope = scope(definition);
            for (final FieldDefinition field : definition.fields()) {
                result = result.and(capabilities(rustType(field.type(), scope)));
            }
        }
        capabilitiesInProgress.remove(name);
        capabilities.put(name, result);
        return result;
    }

    /** Whether a struct field needs a manual {@code Debug} (a function value has none). */
    static boolean hasDebug(final RustType type) {
        return switch (type) {
            case RustType.Function ignored -> false;
            case RustType.VecOf vec -> hasDebug(vec.element());
            case RustType.SetOf set -> hasDebug(set.element());
            case RustType.MapOf map -> hasDebug(map.key()) && hasDebug(map.value());
            case RustType.Pairs pairs -> hasDebug(pairs.key()) && hasDebug(pairs.value());
            case RustType.Optional optional -> hasDebug(optional.inner());
            default -> true;
        };
    }

    // --- supertypes ----------------------------------------------------------------------------------

    /**
     * The traits a type implements, transitively: every abstraction supertype that is not sealed, as seen from the
     * type (with its type arguments), without duplicates.
     */
    List<Type.DeclaredType> traits(final TypeDefinition type) {
        final Map<QualifiedName, Type.DeclaredType> result = new LinkedHashMap<>();
        collectTraits(type, Map.of(), result);
        return List.copyOf(result.values());
    }


    private void collectTraits(final TypeDefinition type, final Map<Type.TypeVariable, Type> substitution,
                               final Map<QualifiedName, Type.DeclaredType> out) {
        for (final Type supertype : type.supertypes()) {
            if (!(supertype instanceof Type.DeclaredType declared) || !isTrait(declared.name())
                    || out.containsKey(declared.name())) {
                continue;
            }
            final Type.DeclaredType seen = (Type.DeclaredType) LinkedModel.substitute(declared, substitution);
            out.put(declared.name(), seen);
            final TypeDefinition definition = model.definition(declared);
            final Map<Type.TypeVariable, Type> next = new HashMap<>();
            for (int i = 0; i < definition.typeParameters().size() && i < seen.arguments().size(); i++) {
                next.put(definition.typeParameters().get(i).variable(), seen.arguments().get(i));
            }
            collectTraits(definition, next, out);
        }
    }

    /** The direct supertraits of a trait (the abstraction supertypes that are not sealed). */
    List<Type.DeclaredType> supertraits(final TypeDefinition type) {
        return type.supertypes().stream().filter(Type.DeclaredType.class::isInstance)
                .map(Type.DeclaredType.class::cast).filter(s -> isTrait(s.name()) && isGenerated(s.name())).toList();
    }

    /** Whether a method has the meaning of {@code Display} ({@code string toString()}). */
    static boolean isDisplay(final MethodDefinition method) {
        return method.name().equals("toString") && method.parameters().isEmpty() && !method.isStatic()
                && method.returnType() instanceof Type.BasicType basic
                && basic.builtin().category() == BuiltinType.Category.STRING;
    }

    // --- names ---------------------------------------------------------------------------------------

    /** The Rust name of a method or function (unique among the overloads). */
    String methodName(final MethodDefinition method) {
        return methodNames.getOrDefault(method.location(), RustNames.member(method.name()));
    }

    /**
     * Names the overloads: per type (including the inherited methods) and per namespace, the overload with the fewest
     * parameters keeps the name, the others get {@code _with_<parameters>}; an overriding method keeps the name of
     * the method it overrides.
     */
    private void planNames() {
        final Set<QualifiedName> done = new HashSet<>();
        model.types().forEach(t -> nameMethods(t, done));
        for (final List<FunctionDefinition> list : functions.values()) {
            final Map<String, List<MethodDefinition>> groups = new LinkedHashMap<>();
            list.forEach(f -> groups.computeIfAbsent(f.method().name(), k -> new ArrayList<>()).add(f.method()));
            groups.values().forEach(group -> assign(group, new HashSet<>()));
        }
    }

    private void nameMethods(final TypeDefinition type, final Set<QualifiedName> done) {
        if (!done.add(type.name())) {
            return;
        }
        for (final Type supertype : type.supertypes()) {
            if (supertype instanceof Type.DeclaredType declared) {
                model.type(declared.name()).ifPresent(s -> nameMethods(s, done));
            }
        }
        final Map<String, List<MethodDefinition>> own = new LinkedHashMap<>();
        final Map<String, Set<String>> taken = new HashMap<>();
        for (final MethodDefinition method : type.methods()) {
            if (!type.name().equals(method.declaringType())) {
                taken.computeIfAbsent(method.name(), k -> new HashSet<>()).add(methodName(method));
                continue;
            }
            final Optional<String> inherited = overridden(type, method);
            if (inherited.isPresent()) {
                methodNames.put(method.location(), inherited.get());
                taken.computeIfAbsent(method.name(), k -> new HashSet<>()).add(inherited.get());
            } else {
                own.computeIfAbsent(method.name(), k -> new ArrayList<>()).add(method);
            }
        }
        // static methods are not inherited and not part of type.methods() of subtypes, but of the type itself
        own.forEach((name, group) -> assign(group, taken.getOrDefault(name, new HashSet<>())));
    }

    /** The name of the inherited method that a declared method overrides, if it does. */
    private Optional<String> overridden(final TypeDefinition type, final MethodDefinition method) {
        for (final Type supertype : type.supertypes()) {
            if (!(supertype instanceof Type.DeclaredType declared)) {
                continue;
            }
            final Optional<TypeDefinition> definition = model.type(declared.name());
            if (definition.isEmpty()) {
                continue;
            }
            for (final MethodDefinition inherited : definition.get().methods()) {
                if (inherited.isStatic() || !inherited.name().equals(method.name())
                        || inherited.parameters().size() != method.parameters().size()) {
                    continue;
                }
                boolean same = true;
                for (int i = 0; i < method.parameters().size() && same; i++) {
                    same = model.substitute(declared, inherited.parameters().get(i).type()).text()
                            .equals(method.parameters().get(i).type().text());
                }
                if (same) {
                    return Optional.of(methodName(inherited));
                }
            }
        }
        return Optional.empty();
    }

    private void assign(final List<MethodDefinition> group, final Set<String> taken) {
        final List<MethodDefinition> ordered = new ArrayList<>(group);
        ordered.sort(Comparator.comparingInt(m -> m.parameters().size()));
        final Set<String> used = new HashSet<>(taken);
        for (final MethodDefinition method : ordered) {
            final String base = RustNames.snake(method.name());
            String name = base;
            if (used.contains(name)) {
                name = method.parameters().isEmpty() ? base + "_0" : base + "_with_" + String.join("_and_",
                        method.parameters().stream().map(p -> RustNames.snake(p.name())).toList());
            }
            final String candidate = name;
            for (int i = 2; used.contains(name); i++) {
                name = candidate + "_" + i;
            }
            used.add(name);
            methodNames.put(method.location(), RustNames.identifier(name));
        }
    }

    // --- errors --------------------------------------------------------------------------------------

    /** The error identifiers a method declares, in order. */
    static List<String> errorIds(final MethodDefinition method) {
        return method.annotations().stream().filter(a -> a.name().equals("throws"))
                .flatMap(a -> a.arguments().stream()).map(Literal::text).distinct().toList();
    }

    ErrorType error(final String errorId) {
        return Objects.requireNonNull(errors.get(errorId), () -> "no error type for " + errorId);
    }

    /**
     * The error enum of a method or function with several errors.
     *
     * @param method the method
     * @return the enum, empty if the method declares at most one error type
     */
    Optional<ErrorEnum> errorEnum(final MethodDefinition method) {
        return Optional.ofNullable(errorEnums.get(method.location()));
    }

    private void planErrors() {
        final Map<String, Set<String>> usage = new TreeMap<>();
        final List<Map.Entry<MethodDefinition, String>> methods = new ArrayList<>();
        for (final TypeDefinition type : model.types()) {
            if (generated.contains(type.name())) {
                for (final MethodDefinition method : type.declaredMethods()) {
                    methods.add(Map.entry(method, type.name().namespace()));
                }
            }
        }
        functions().forEach(f -> methods.add(Map.entry(f.method(), f.namespace())));
        for (final Map.Entry<MethodDefinition, String> entry : methods) {
            errorIds(entry.getKey()).forEach(id -> usage.computeIfAbsent(id, k -> new TreeSet<>())
                    .add(entry.getValue()));
        }
        final List<String> problems = new ArrayList<>();
        usage.forEach((errorId, namespaces) -> {
            if (STANDARD_ERRORS.contains(errorId)) {
                errors.put(errorId, new ErrorType(null, "InvalidArgumentError"));
                return;
            }
            final Set<String> users = new TreeSet<>();
            namespaces.forEach(n -> users.add(folder(n)));
            final Optional<String> home = namespaces.stream()
                    .filter(n -> users.stream().allMatch(u -> u.equals(folder(n))
                            || SpecFolders.required(u, folders).contains(folder(n))))
                    .min(Comparator.comparing(String::length).thenComparing(n -> n));
            if (home.isEmpty()) {
                problems.add("Error '" + errorId + "' is used in the crates " + users + ", but none of them is "
                        + "required by all others; its error type has no home");
            } else {
                errors.put(errorId, new ErrorType(home.get(), RustNames.errorType(errorId)));
            }
        });
        if (!problems.isEmpty()) {
            throw new GenerationException(problems);
        }
        for (final Map.Entry<MethodDefinition, String> entry : methods) {
            final MethodDefinition method = entry.getKey();
            final List<String> ids = errorIds(method);
            final Map<String, ErrorType> types = new LinkedHashMap<>();
            ids.forEach(id -> types.putIfAbsent(id, error(id)));
            if (new HashSet<>(types.values()).size() > 1) {
                final String owner = method.declaringType() == null ? "" : method.declaringType().name();
                errorEnums.put(method.location(), new ErrorEnum(entry.getValue(), owner
                        + RustNames.pascal(methodName(method).replace("r#", "")) + "Error", types));
            }
        }
    }

    // --- planning ------------------------------------------------------------------------------------

    /** Finds the erased parameters (used with {@code ANY}) and the {@code $$Self} parameters of traits. */
    private void planParameters() {
        final Consumer<Type> scan = new Consumer<>() {
            @Override
            public void accept(final Type type) {
                switch (type) {
                    case Type.DeclaredType declared -> {
                        for (int i = 0; i < declared.arguments().size(); i++) {
                            final Type argument = declared.arguments().get(i);
                            if (argument instanceof Type.AnyType || argument instanceof Type.WildcardType) {
                                erased.computeIfAbsent(declared.name(), k -> new TreeSet<>()).add(i);
                            }
                            accept(argument);
                        }
                    }
                    case Type.BasicType basic -> basic.arguments().forEach(this);
                    case Type.WildcardType wildcard -> {
                        if (wildcard.upperBound() != null) {
                            accept(wildcard.upperBound());
                        }
                    }
                    case Type.FunctionType function -> {
                        accept(function.returnType());
                        function.parameters().forEach(p -> accept(p.type()));
                    }
                    default -> {
                    }
                }
            }
        };
        for (final TypeDefinition type : model.types()) {
            referenced(type).forEach(scan);
            for (int i = 0; i < type.typeParameters().size(); i++) {
                final Type bound = type.typeParameters().get(i).bound();
                if (isAbstraction(type.name()) && bound instanceof Type.DeclaredType declared
                        && declared.name().equals(type.name())
                        && declared.arguments().contains(type.typeParameters().get(i).variable())) {
                    selfParameter.put(type.name(), i);
                }
            }
        }
        for (final FunctionDefinition function : model.functions()) {
            methodTypes(function.method()).forEach(scan);
        }
        model.constants().forEach(c -> scan.accept(c.type()));
        model.instances().stream().map(InstanceDefinition::type).forEach(scan);
        // the $$Self parameter is never erased: it is Self
        selfParameter.forEach((type, index) -> erased.getOrDefault(type, new HashSet<>()).remove(index));
    }

    /** The types that can be generated: a greatest fixed point over the types with a Rust mapping. */
    private Set<QualifiedName> plan() {
        final Set<QualifiedName> candidates = new TreeSet<>();
        // every namespace belongs to a folder (SpecFolders rejects other specs)
        model.types().forEach(type -> candidates.add(type.name()));
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
            final Optional<String> reason = problem(methodTypes(function.method()), generated);
            if (reason.isPresent()) {
                deferred.put(new QualifiedName(function.namespace(), function.method().signature()), reason.get());
            } else {
                functions.computeIfAbsent(function.namespace(), k -> new ArrayList<>()).add(function);
            }
        }
        for (final ConstantDefinition constant : model.constants()) {
            final Optional<String> reason = problem(List.of(constant.type()), generated);
            if (reason.isPresent()) {
                deferred.put(constant.name(), reason.get());
            } else {
                constants.computeIfAbsent(constant.name().namespace(), k -> new ArrayList<>()).add(constant);
            }
        }
    }

    /** Every type a type declaration refers to. */
    static List<Type> referenced(final TypeDefinition type) {
        final List<Type> types = new ArrayList<>(type.supertypes());
        type.typeParameters().stream().filter(p -> p.bound() != null).forEach(p -> types.add(p.bound()));
        type.fields().stream().map(FieldDefinition::type).forEach(types::add);
        type.methods().forEach(m -> types.addAll(methodTypes(m)));
        type.declaredMethods().forEach(m -> types.addAll(methodTypes(m)));
        return types;
    }

    static List<Type> methodTypes(final MethodDefinition method) {
        final List<Type> types = new ArrayList<>();
        method.typeParameters().stream().filter(p -> p.bound() != null).forEach(p -> types.add(p.bound()));
        types.add(method.returnType());
        method.parameters().stream().map(ParameterDefinition::type).forEach(types::add);
        return types;
    }

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
                    + "' has no Rust mapping yet");
            case Type.DeclaredType declared -> available.contains(declared.name())
                    ? problem(declared.arguments(), available)
                    : Optional.of("refers to " + declared.name() + " (not generated yet)");
            case Type.BasicType basic -> basic.builtin().category() == BuiltinType.Category.INTEGER
                    && (basic.builtin().bits() <= 0 || basic.builtin().bits() > 256)
                    ? Optional.of("Integer type '" + basic.text() + "' has no Rust mapping")
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
