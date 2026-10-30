package org.hiero.sdk.v3.metalang.tck;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.diagnostic.Severity;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;
import org.jspecify.annotations.Nullable;

/**
 * Reads the bindings files of the TCK and resolves them against the linked model of the specs: every target type and
 * attribute must exist, every converter must exist, work in the right direction and fit the attribute's type, and
 * every result path must lead to a field of the receipt (transactions) or of the response (queries). The resolved
 * bindings are the input of the TCK server generators of the languages.
 *
 * <p>The execution flow is the one of the TCK: a transaction is created with its attributes, gets the common
 * transaction parameters, is sent with {@code signWithOperatorAndSubmit} and answered with its receipt
 * ({@code queryReceipt}); a query is sent with {@code submit} and answered with the value of its response. A call of
 * a method of a repository of the Mirror Node client ({@code -> Repository::method}) passes the bound parameters and
 * is answered with the value the method returns; a page is read up to the end or up to the limit of {@code take}.
 */
public final class TckBindings {

    /** The kind of a binding, derived from its target type. */
    public enum Kind {
        /** a transaction: answered with its receipt */
        TRANSACTION,
        /** a query: answered with its response */
        QUERY,
        /** the common transaction parameters: setters of mutable attributes */
        COMMON,
        /** a method of a repository of the Mirror Node client: answered with the value it returns */
        MIRROR
    }

    /**
     * A resolved binding.
     *
     * @param binding    the binding as written
     * @param file       the bindings file (for grouping in the generated code)
     * @param kind       the kind
     * @param type       the target type
     * @param members    the resolved members
     * @param resultType the receipt type (transactions) or the response value type (queries), {@code null} for the
     *                   common parameters and for a call of a method
     * @param call       the called method of a repository, {@code null} for the other kinds
     */
    public record Resolved(Binding binding, String file, Kind kind, TypeDefinition type, List<Member> members,
                           @Nullable TypeDefinition resultType, @Nullable Call call) {

        /**
         * Creates a resolved binding.
         *
         * @param binding    the binding
         * @param file       the file
         * @param kind       the kind
         * @param type       the target type
         * @param members    the members
         * @param resultType the result type
         * @param call       the called method
         */
        public Resolved {
            Objects.requireNonNull(binding, "binding must not be null");
            members = List.copyOf(members);
        }

        /**
         * Creates a resolved binding of a request.
         *
         * @param binding    the binding
         * @param file       the file
         * @param kind       the kind
         * @param type       the target type
         * @param members    the members
         * @param resultType the result type
         */
        public Resolved(final Binding binding, final String file, final Kind kind, final TypeDefinition type,
                        final List<Member> members, final @Nullable TypeDefinition resultType) {
            this(binding, file, kind, type, members, resultType, null);
        }
    }

    /**
     * The method of a repository of the Mirror Node client that a binding calls.
     *
     * @param repository the attribute of the Mirror Node client that holds the repository
     * @param method     the called overload of the method
     * @param paged      whether the method returns a page: the result is then the list of the items of all pages
     *                   that are read
     * @param response   the type of {@code response}: the returned type, or {@code list<T>} for a page of {@code T}
     */
    public record Call(FieldDefinition repository, MethodDefinition method, boolean paged, Type response) {
    }

    /** A resolved member of a binding. */
    public sealed interface Member {
    }

    /** How a converter is applied: to the value itself, or to every element of a list or set. */
    public enum Collection {
        /** the value itself */
        NONE,
        /** every element of a {@code list} */
        LIST,
        /** every element of a {@code set} */
        SET
    }

    /**
     * An attribute with its JSON sources.
     *
     * @param field      the attribute
     * @param sources    the alternative sources with their converters
     * @param collection whether the converter converts the elements of a list or set
     */
    public record Value(FieldDefinition field, List<Source> sources, Collection collection) implements Member {

        /**
         * Creates a value.
         *
         * @param field      the attribute
         * @param sources    the sources
         * @param collection the collection
         */
        public Value {
            sources = List.copyOf(sources);
        }

        /**
         * Whether the attribute is set with a setter (Java) or an assignment (TypeScript) after the request was
         * created instead of being passed to its constructor: a mutable attribute that may be absent.
         *
         * @return {@code true} for a setter
         */
        public boolean setter() {
            return TckBindings.setter(field);
        }
    }

    /**
     * A JSON source with its converter.
     *
     * @param path      the JSON path, {@code null} for a constant
     * @param literal   the constant, {@code null} for a JSON path
     * @param converter the converter
     */
    public record Source(Binding.@Nullable Path path, @Nullable String literal, Converter converter) {
    }

    /**
     * A list attribute built from JSON lists.
     *
     * @param field       the list attribute
     * @param generators  the iterations
     * @param elementType the element type
     * @param members     the values of the element type
     */
    public record Elements(FieldDefinition field, List<Binding.Generator> generators, TypeDefinition elementType,
                           List<Member> members) implements Member {

        /**
         * Creates a list mapping.
         *
         * @param field       the attribute
         * @param generators  the iterations
         * @param elementType the element type
         * @param members     the members
         */
        public Elements {
            generators = List.copyOf(generators);
            members = List.copyOf(members);
        }
    }

    /**
     * An attribute of a declared type created from a JSON object.
     *
     * @param field    the attribute
     * @param path     the JSON object, {@code null} for the current one
     * @param variants the types to create, selected by the presence of a JSON parameter
     */
    public record Nested(FieldDefinition field, Binding.@Nullable Path path, List<Variant> variants)
            implements Member {

        /**
         * Creates a nested value.
         *
         * @param field    the attribute
         * @param path     the JSON object
         * @param variants the variants
         */
        public Nested {
            variants = List.copyOf(variants);
        }
    }

    /**
     * A resolved variant of a nested value.
     *
     * @param when    the JSON parameter that selects it, {@code null} for the only or the {@code else} variant
     * @param type    the type
     * @param members the members
     */
    public record Variant(Binding.@Nullable Path when, TypeDefinition type, List<Member> members) {

        /**
         * Creates a variant.
         *
         * @param when    the condition
         * @param type    the type
         * @param members the members
         */
        public Variant {
            members = List.copyOf(members);
        }
    }

    /**
     * A transaction attribute built from a nested TCK request ({@code method} and {@code params}).
     *
     * @param field the attribute
     * @param path  the JSON object of the request
     */
    public record Request(FieldDefinition field, Binding.Path path) implements Member {
    }

    /**
     * A parameter of the called method with its JSON sources. A list parameter (the conditions of a query parameter
     * of the Mirror Node) also accepts a single JSON value as list of one element; without a source it is empty.
     *
     * @param parameter  the parameter
     * @param sources    the alternative sources with their converters
     * @param collection whether the converter converts the elements of a list or set
     */
    public record Argument(ParameterDefinition parameter, List<Source> sources, Collection collection)
            implements Member {

        /**
         * Creates an argument.
         *
         * @param parameter  the parameter
         * @param sources    the sources
         * @param collection the collection
         */
        public Argument {
            sources = List.copyOf(sources);
        }
    }

    /**
     * The maximum number of items of a paged result, from a JSON parameter.
     *
     * @param path the JSON parameter
     */
    public record Take(Binding.Path path) implements Member {
    }

    /**
     * A JSON list of the result with one object per element of a list value.
     *
     * @param name        the result field
     * @param fields      the attributes to the list, from the receipt, the response or the enclosing element
     * @param elementType the type of the elements
     * @param members     the results of an element (relative to it) and unsupported results
     */
    public record ResultList(Binding.Path name, List<FieldDefinition> fields, TypeDefinition elementType,
                             List<Member> members) implements Member {

        /**
         * Creates a list of result objects.
         *
         * @param name        the result field
         * @param fields      the attribute path
         * @param elementType the element type
         * @param members     the members
         */
        public ResultList {
            fields = List.copyOf(fields);
            members = List.copyOf(members);
        }
    }

    /**
     * A field of the JSON result.
     *
     * @param name       the result field (a path for nested objects)
     * @param fields     the attributes from the receipt or response to the value, empty for the whole value
     * @param converter  the converter to JSON
     * @param collection whether the converter converts the elements of a list or set
     */
    public record Result(Binding.Path name, List<FieldDefinition> fields, Converter converter, Collection collection)
            implements Member {

        /**
         * Creates a result.
         *
         * @param name       the result field
         * @param fields     the attribute path
         * @param converter  the converter
         * @param collection the collection
         */
        public Result {
            fields = List.copyOf(fields);
        }
    }

    /**
     * A parameter or result field the API cannot provide.
     *
     * @param unsupported the declaration
     */
    public record Unsupported(Binding.Unsupported unsupported) implements Member {
    }

    /**
     * The bindings of all files.
     *
     * @param bindings    the method bindings, sorted by file and file order
     * @param common      the binding of the common transaction parameters, if declared
     * @param diagnostics the problems, sorted
     */
    public record Bindings(List<Resolved> bindings, Optional<Resolved> common, List<Diagnostic> diagnostics) {

        /**
         * Creates the bindings.
         *
         * @param bindings    the method bindings
         * @param common      the common binding
         * @param diagnostics the problems
         */
        public Bindings {
            bindings = List.copyOf(bindings);
            diagnostics = diagnostics.stream().sorted().toList();
        }

        /** Whether there is an error. */
        public boolean hasErrors() {
            return diagnostics.stream().anyMatch(d -> d.severity() == Severity.ERROR);
        }
    }

    private final LinkedModel model;
    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private Map<String, QualifiedName> imports = Map.of();

    private TckBindings(final LinkedModel model) {
        this.model = model;
    }

    /**
     * Reads all Markdown files of a directory (recursively, sorted) and resolves their bindings.
     *
     * @param directory the directory of the bindings files
     * @param model     the linked model of the specs
     * @return the resolved bindings
     * @throws IOException if a file cannot be read
     */
    public static Bindings read(final Path directory, final LinkedModel model) throws IOException {
        final Map<String, String> files = new TreeMap<>();
        try (Stream<Path> paths = Files.walk(directory)) {
            for (final Path path : paths.filter(p -> p.toString().endsWith(".md")).sorted().toList()) {
                files.put(directory.relativize(path).toString().replace('\\', '/'),
                        Files.readString(path, StandardCharsets.UTF_8));
            }
        }
        return resolve(files, model);
    }

    /**
     * Resolves the bindings of Markdown files.
     *
     * @param files the files: name to content
     * @param model the linked model of the specs
     * @return the resolved bindings
     */
    public static Bindings resolve(final Map<String, String> files, final LinkedModel model) {
        final TckBindings resolver = new TckBindings(model);
        final List<Resolved> bindings = new ArrayList<>();
        Resolved common = null;
        final Set<String> methods = new HashSet<>();
        for (final Map.Entry<String, String> file : new TreeMap<>(files).entrySet()) {
            final BindingParser.Result parsed = BindingParser.parse(file.getKey(), file.getValue());
            resolver.diagnostics.addAll(parsed.diagnostics());
            resolver.imports = resolver.imports(parsed.requires());
            Binding previous = null;
            for (final Binding binding : parsed.bindings()) {
                final boolean variant = previous != null && !binding.common() && !previous.common()
                        && previous.name().equals(binding.name()) && previous.when() != null;
                previous = binding;
                final Optional<Resolved> resolved = resolver.binding(binding, file.getKey());
                if (binding.common()) {
                    if (resolved.isEmpty()) {
                        continue;
                    }
                    if (common != null) {
                        resolver.error("tck.duplicate", "The common transaction parameters are bound twice",
                                binding.location());
                    }
                    common = resolved.get();
                    continue;
                }
                if (!methods.add(binding.name()) && !variant) {
                    resolver.error("tck.duplicate", "Method '" + binding.name() + "' is bound twice",
                            binding.location());
                    continue;
                }
                if (resolved.isEmpty()) {
                    continue;
                }
                final Optional<Resolved> first = bindings.stream()
                        .filter(b -> b.binding().name().equals(binding.name())).findFirst();
                if (first.isPresent() && first.get().kind() != resolved.get().kind()) {
                    resolver.error("tck.kind", "The variants of method '" + binding.name() + "' are not all "
                            + first.get().kind().name().toLowerCase(Locale.ROOT) + "s", binding.location());
                    continue;
                }
                bindings.add(resolved.get());
            }
        }
        return new Bindings(bindings, Optional.ofNullable(common), resolver.diagnostics);
    }

    /**
     * Whether an attribute is set with a setter after the request was created instead of being passed to its
     * constructor: a mutable attribute that may be absent ({@code @@nullable} or {@code @@default}).
     *
     * @param field the attribute
     * @return {@code true} for a setter
     */
    public static boolean setter(final FieldDefinition field) {
        return !field.hasAnnotation("immutable") && (field.hasAnnotation("nullable") || field.hasAnnotation("default"));
    }

    private Map<String, QualifiedName> imports(final List<BindingParser.Requires> requires) {
        final Map<String, QualifiedName> result = new HashMap<>();
        for (final BindingParser.Requires require : requires) {
            for (final String name : require.names()) {
                final QualifiedName qualified = new QualifiedName(require.namespace(), name);
                if (model.type(qualified).isEmpty()) {
                    error("tck.unknown-type", "Type '" + qualified + "' does not exist", require.location());
                } else {
                    result.put(name, qualified);
                }
            }
        }
        return result;
    }

    private Optional<TypeDefinition> type(final String name, final SourceLocation location) {
        final int dot = name.lastIndexOf('.');
        final QualifiedName qualified = dot < 0 ? imports.get(name)
                : new QualifiedName(name.substring(0, dot), name.substring(dot + 1));
        final Optional<TypeDefinition> type = qualified == null ? Optional.empty() : model.type(qualified);
        if (type.isEmpty()) {
            error("tck.unknown-type", "Type '" + name + "' is not imported or does not exist", location);
        }
        return type;
    }

    private Optional<Resolved> binding(final Binding binding, final String file) {
        final Optional<TypeDefinition> type = type(binding.type(), binding.location());
        if (type.isEmpty()) {
            return Optional.empty();
        }
        if (binding.method() != null) {
            return call(binding, file, type.get());
        }
        final Kind kind;
        final TypeDefinition resultType;
        if (binding.common()) {
            kind = Kind.COMMON;
            resultType = null;
            if (binding.when() != null) {
                error("tck.syntax", "The common transaction parameters have no variants", binding.location());
            }
        } else {
            final Optional<TypeDefinition> receipt = resultOf(type.get(), "signWithOperatorAndSubmit");
            final Optional<TypeDefinition> response = resultOf(type.get(), "submit");
            if (receipt.isPresent()) {
                kind = Kind.TRANSACTION;
                resultType = receipt.get();
            } else if (response.isPresent()) {
                kind = Kind.QUERY;
                resultType = response.get();
            } else {
                error("tck.kind", "Type '" + binding.type() + "' is neither a transaction nor a query",
                        binding.location());
                return Optional.empty();
            }
        }
        final List<Member> members = members(binding.members(), type.get(), kind, resultType, Scope.TOP);
        if (kind == Kind.TRANSACTION || kind == Kind.QUERY) {
            required(binding.members(), type.get(), binding.location());
        }
        return Optional.of(new Resolved(binding, file, kind, type.get(), members, resultType));
    }

    /** The Mirror Node client, whose attributes hold the repositories a binding can call. */
    private static final QualifiedName MIRROR_NODE_CLIENT = new QualifiedName("mirrornode", "MirrorNodeClient");

    /** The type of a page of a paginated result. */
    private static final QualifiedName PAGE = new QualifiedName("common", "Page");

    /**
     * Resolves the call of a method of a repository: the repository must be an attribute of the Mirror Node client,
     * the method is the overload with the fewest parameters that has all bound ones.
     */
    private Optional<Resolved> call(final Binding binding, final String file, final TypeDefinition type) {
        final String name = Objects.requireNonNull(binding.method());
        if (binding.common()) {
            error("tck.syntax", "The common transaction parameters call no method", binding.location());
            return Optional.empty();
        }
        final Optional<FieldDefinition> repository = model.type(MIRROR_NODE_CLIENT).stream()
                .flatMap(client -> client.fields().stream())
                .filter(f -> f.type() instanceof Type.DeclaredType declared && declared.name().equals(type.name()))
                .findFirst();
        if (repository.isEmpty()) {
            error("tck.kind", "Type '" + binding.type() + "' is no repository of " + MIRROR_NODE_CLIENT.name()
                    + "; only its methods can be called", binding.location());
            return Optional.empty();
        }
        final Set<String> bound = new HashSet<>();
        binding.members().forEach(m -> {
            if (m instanceof Binding.Assignment assignment) {
                bound.add(assignment.attribute());
            }
        });
        final List<MethodDefinition> overloads = type.methods(name).stream().filter(m -> !m.isStatic()).toList();
        if (overloads.isEmpty()) {
            error("tck.unknown-attribute", "Type '" + type.name() + "' has no method '" + name + "'",
                    binding.location());
            return Optional.empty();
        }
        final Optional<MethodDefinition> method = overloads.stream()
                .filter(m -> m.parameters().stream().map(ParameterDefinition::name).toList().containsAll(bound))
                .min(java.util.Comparator.comparingInt(m -> m.parameters().size()));
        if (method.isEmpty()) {
            error("tck.unknown-attribute", "No method '" + name + "' of '" + type.name() + "' has the parameters "
                    + new java.util.TreeSet<>(bound), binding.location());
            return Optional.empty();
        }
        final Type returned = method.get().returnType();
        final boolean paged = returned instanceof Type.DeclaredType declared && declared.name().equals(PAGE);
        final Type response = paged ? new Type.BasicType(BuiltinType.lookup("list").orElseThrow(),
                List.of(((Type.DeclaredType) returned).arguments().getFirst())) : returned;
        final Call call = new Call(repository.get(), method.get(), paged, response);
        final List<Member> members = new ArrayList<>();
        final Set<String> assigned = new HashSet<>();
        final Set<String> results = new HashSet<>();
        boolean take = false;
        for (final Binding.Member member : binding.members()) {
            switch (member) {
                case Binding.Assignment assignment -> argument(method.get(), assignment, assigned)
                        .ifPresent(members::add);
                case Binding.Take limit -> {
                    if (!paged) {
                        error("tck.syntax", "'take' needs a method that returns a page", limit.location());
                    } else if (take) {
                        error("tck.duplicate", "'take' is bound twice", limit.location());
                    } else if (limit.path().parent()) {
                        error("tck.parent", "'" + limit.path() + "' refers to an enclosing element", limit.location());
                    } else {
                        members.add(new Take(limit.path()));
                    }
                    take = true;
                }
                case Binding.Result value -> {
                    if (!results.add(value.name().toString())) {
                        error("tck.duplicate", "Result '" + value.name() + "' is bound twice", value.location());
                    } else {
                        result(value, root(Kind.MIRROR), response).ifPresent(members::add);
                    }
                }
                case Binding.ResultList list -> {
                    if (!results.add(list.name().toString())) {
                        error("tck.duplicate", "Result '" + list.name() + "' is bound twice", list.location());
                    } else {
                        resultList(list, root(Kind.MIRROR), response).ifPresent(members::add);
                    }
                }
                case Binding.Unsupported unsupported -> members.add(new Unsupported(unsupported));
                default -> error("tck.syntax", "A call of a method has only parameters, 'take' and results",
                        member.location());
            }
        }
        for (final ParameterDefinition parameter : method.get().parameters()) {
            if (!assigned.contains(parameter.name()) && !parameter.hasAnnotation("nullable")
                    && collection(parameter.type()) == Collection.NONE) {
                error("tck.required", "Required parameter '" + parameter.name() + "' of '" + type.name() + "::"
                        + name + "' gets no value", binding.location());
            }
        }
        return Optional.of(new Resolved(binding, file, Kind.MIRROR, type, members, null, call));
    }

    private Optional<Argument> argument(final MethodDefinition method, final Binding.Assignment assignment,
                                        final Set<String> assigned) {
        final Optional<ParameterDefinition> parameter = method.parameters().stream()
                .filter(p -> p.name().equals(assignment.attribute())).findFirst();
        if (parameter.isEmpty()) {
            error("tck.unknown-attribute", "Method '" + method.name() + "' has no parameter '"
                    + assignment.attribute() + "'", assignment.location());
            return Optional.empty();
        }
        if (!assigned.add(assignment.attribute())) {
            error("tck.duplicate", "Parameter '" + assignment.attribute() + "' is bound twice", assignment.location());
            return Optional.empty();
        }
        final Collection collection = collection(parameter.get().type());
        final Type valueType = collection == Collection.NONE ? parameter.get().type()
                : ((Type.BasicType) parameter.get().type()).arguments().getFirst();
        final List<Source> sources = new ArrayList<>();
        for (final Binding.Source source : assignment.sources()) {
            if (source.path() != null && source.path().parent()) {
                error("tck.parent", "'" + source.path() + "' refers to an enclosing element", assignment.location());
                continue;
            }
            converter(source.converter(), true, valueType, assignment.location())
                    .ifPresent(c -> sources.add(new Source(source.path(), source.literal(), c)));
        }
        return Optional.of(new Argument(parameter.get(), sources, collection));
    }

    /** The receipt or response type: the first type argument of the result of the method that sends the request. */
    private Optional<TypeDefinition> resultOf(final TypeDefinition type, final String method) {
        return type.methods(method).stream().filter(m -> !m.isStatic()).map(MethodDefinition::returnType)
                .filter(Type.DeclaredType.class::isInstance).map(Type.DeclaredType.class::cast)
                .filter(t -> !t.arguments().isEmpty() && t.arguments().getFirst() instanceof Type.DeclaredType)
                .map(t -> model.definition((Type.DeclaredType) t.arguments().getFirst())).findFirst();
    }

    /**
     * Where members are resolved.
     *
     * @param top    whether the members belong to the type of the binding (setters are allowed there)
     * @param parent whether {@code ^} refers to something (inside {@code each} over objects or a nested object)
     * @param names  the names bound by {@code each name in list}
     */
    private record Scope(boolean top, boolean parent, Set<String> names) {

        static final Scope TOP = new Scope(true, false, Set.of());

        Scope nested(final boolean parent) {
            return new Scope(false, this.parent || parent, names);
        }
    }

    private List<Member> members(final List<Binding.Member> members, final TypeDefinition type, final Kind kind,
                                 final @Nullable TypeDefinition resultType, final Scope scope) {
        final List<Member> result = new ArrayList<>();
        final Set<String> attributes = new HashSet<>();
        final Set<String> results = new HashSet<>();
        for (final Binding.Member member : members) {
            switch (member) {
                case Binding.Assignment assignment -> attribute(type, assignment.attribute(), kind, scope,
                        assignment.location(), attributes).ifPresent(field -> {
                    final Collection collection = collection(field.type());
                    final Type valueType = collection == Collection.NONE ? field.type()
                            : ((Type.BasicType) field.type()).arguments().getFirst();
                    final List<Source> sources = new ArrayList<>();
                    for (final Binding.Source source : assignment.sources()) {
                        if (source.path() != null && source.path().parent() && !scope.parent()) {
                            error("tck.parent", "'" + source.path() + "' refers to the enclosing element, but '"
                                    + assignment.attribute() + "' is not inside 'each'", assignment.location());
                            continue;
                        }
                        converter(source.converter(), true, valueType, assignment.location())
                                .ifPresent(c -> sources.add(new Source(source.path(), source.literal(), c)));
                    }
                    result.add(new Value(field, sources, collection));
                });
                case Binding.Each each -> attribute(type, each.attribute(), kind, scope, each.location(), attributes)
                        .ifPresent(field -> elements(field, each, scope).ifPresent(result::add));
                case Binding.Construct construct -> attribute(type, construct.attribute(), kind, scope,
                        construct.location(), attributes)
                        .ifPresent(field -> nested(field, construct, scope).ifPresent(result::add));
                case Binding.Request request -> attribute(type, request.attribute(), kind, scope,
                        request.location(), attributes).ifPresent(field -> {
                    if (field.type() instanceof Type.DeclaredType declared
                            && declared.name().equals(TRANSACTION)) {
                        result.add(new Request(field, request.path()));
                    } else {
                        error("tck.converter", "Attribute '" + field.name() + "' is no " + TRANSACTION
                                + "; 'request' builds one", request.location());
                    }
                });
                case Binding.Result value -> {
                    if (resultType == null) {
                        error("tck.result", kind == Kind.COMMON ? "The common transaction parameters have no result"
                                : "Only the binding itself has results", value.location());
                    } else if (!results.add(value.name().toString())) {
                        error("tck.duplicate", "Result '" + value.name() + "' is bound twice", value.location());
                    } else {
                        result(value, root(kind), declared(resultType)).ifPresent(result::add);
                    }
                }
                case Binding.ResultList list -> {
                    if (resultType == null) {
                        error("tck.result", kind == Kind.COMMON ? "The common transaction parameters have no result"
                                : "Only the binding itself has results", list.location());
                    } else if (!results.add(list.name().toString())) {
                        error("tck.duplicate", "Result '" + list.name() + "' is bound twice", list.location());
                    } else {
                        resultList(list, root(kind), declared(resultType)).ifPresent(result::add);
                    }
                }
                case Binding.Take take -> error("tck.syntax", "'take' needs a method that returns a page",
                        take.location());
                case Binding.Unsupported unsupported -> result.add(new Unsupported(unsupported));
            }
        }
        return result;
    }

    /** The type of the transactions a {@code request} builds. */
    private static final QualifiedName TRANSACTION = new QualifiedName("consensusnode.transactions", "Transaction");

    private static Collection collection(final Type type) {
        if (type instanceof Type.BasicType basic && basic.builtin().category() == BuiltinType.Category.COLLECTION) {
            return basic.builtin().name().equals("set") ? Collection.SET : Collection.LIST;
        }
        return Collection.NONE;
    }

    private Optional<FieldDefinition> attribute(final TypeDefinition type, final String name, final Kind kind,
                                                final Scope scope, final SourceLocation location,
                                                final Set<String> assigned) {
        final Optional<FieldDefinition> field = type.field(name);
        if (field.isEmpty()) {
            error("tck.unknown-attribute", "Type '" + type.name() + "' has no attribute '" + name + "'", location);
            return Optional.empty();
        }
        if (!assigned.add(name)) {
            error("tck.duplicate", "Attribute '" + name + "' is bound twice", location);
            return Optional.empty();
        }
        if (kind == Kind.COMMON && field.get().hasAnnotation("immutable")) {
            error("tck.immutable", "Attribute '" + name + "' of '" + type.name() + "' is immutable; the common "
                    + "transaction parameters can only set mutable attributes", location);
            return Optional.empty();
        }
        if (kind != Kind.COMMON && !scope.top() && setter(field.get())) {
            error("tck.immutable", "Mutable attribute '" + name + "' of '" + type.name() + "' can only be set on "
                    + "the type of the binding itself", location);
            return Optional.empty();
        }
        return field;
    }

    private Optional<Elements> elements(final FieldDefinition field, final Binding.Each each, final Scope scope) {
        if (!(field.type() instanceof Type.BasicType list) || list.builtin().category() != BuiltinType.Category.COLLECTION
                || !(list.arguments().getFirst() instanceof Type.DeclaredType elementType)) {
            error("tck.converter", "Attribute '" + field.name() + "' is no list of a declared type; 'each' needs one",
                    each.location());
            return Optional.empty();
        }
        final Optional<TypeDefinition> type = type(each.type(), each.location());
        if (type.isEmpty()) {
            return Optional.empty();
        }
        if (!type.get().name().equals(elementType.name())) {
            error("tck.converter", "Attribute '" + field.name() + "' holds '" + elementType.name() + "', not '"
                    + type.get().name() + "'", each.location());
            return Optional.empty();
        }
        boolean parent = scope.parent();
        final Set<String> names = new HashSet<>(scope.names());
        for (final Binding.Generator generator : each.generators()) {
            if (generator.path().parent() && !parent) {
                error("tck.parent", "'" + generator.path() + "' refers to the enclosing element, but '"
                        + field.name() + "' is not inside 'each'", each.location());
            }
            if (generator.name() == null) {
                parent = true;
                names.clear(); // the elements of the list replace the JSON object of the names
            } else if (!names.add(generator.name())) {
                error("tck.duplicate", "Name '" + generator.name() + "' is bound twice", each.location());
            }
        }
        final List<Member> members = members(each.members(), type.get(), Kind.TRANSACTION, null,
                new Scope(false, parent, Set.copyOf(names)));
        required(each.members(), type.get(), each.location());
        return Optional.of(new Elements(field, each.generators(), type.get(), members));
    }

    private Optional<Nested> nested(final FieldDefinition field, final Binding.Construct construct,
                                    final Scope scope) {
        if (!(field.type() instanceof Type.DeclaredType declared)) {
            error("tck.converter", "Attribute '" + field.name() + "' is no declared type; '->' creates one",
                    construct.location());
            return Optional.empty();
        }
        if (construct.path() != null && construct.path().parent() && !scope.parent()) {
            error("tck.parent", "'" + construct.path() + "' refers to the enclosing element, but '"
                    + field.name() + "' is not inside 'each'", construct.location());
        }
        final List<Variant> variants = new ArrayList<>();
        for (int i = 0; i < construct.variants().size(); i++) {
            final Binding.Variant variant = construct.variants().get(i);
            final Optional<TypeDefinition> type = type(variant.type(), variant.location());
            if (type.isEmpty()) {
                continue;
            }
            if (!model.isSubtypeOf(type.get().name(), declared.name())) {
                error("tck.converter", "Attribute '" + field.name() + "' holds '" + declared.name() + "', not '"
                        + type.get().name() + "'", variant.location());
                continue;
            }
            final List<Member> members = members(variant.members(), type.get(), Kind.TRANSACTION, null,
                    scope.nested(construct.path() != null));
            members.stream().filter(m -> m instanceof Elements || m instanceof Nested && construct.path() != null)
                    .findFirst().ifPresent(m -> error("tck.syntax", "A nested object cannot contain 'each' or "
                            + "another nested object with a path", variant.location()));
            required(variant.members(), type.get(), variant.location());
            variants.add(new Variant(variant.when(), type.get(), members));
        }
        return variants.size() == construct.variants().size()
                ? Optional.of(new Nested(field, construct.path(), variants)) : Optional.empty();
    }

    /** Reports the required attributes (no null, no default) that get no value: requests cannot be built then. */
    private void required(final List<Binding.Member> members, final TypeDefinition type,
                          final SourceLocation location) {
        final Set<String> assigned = new HashSet<>();
        members.forEach(m -> {
            switch (m) {
                case Binding.Assignment a -> assigned.add(a.attribute());
                case Binding.Each e -> assigned.add(e.attribute());
                case Binding.Construct c -> assigned.add(c.attribute());
                case Binding.Request r -> assigned.add(r.attribute());
                default -> {
                }
            }
        });
        for (final FieldDefinition field : type.fields()) {
            if (field.hasAnnotation("immutable") && !field.hasAnnotation("nullable") && !field.hasAnnotation("default")
                    && !assigned.contains(field.name())) {
                error("tck.required", "Required attribute '" + field.name() + "' of '" + type.name()
                        + "' gets no value", location);
            }
        }
    }

    /**
     * The value results start with.
     *
     * @param name  {@code receipt} or {@code response}
     * @param owner what the results belong to, for messages
     */
    private record Root(String name, String owner) {
    }

    /** The value results start with: {@code receipt} (transactions) or {@code response}. */
    private static Root root(final Kind kind) {
        return switch (kind) {
            case TRANSACTION -> new Root("receipt", "a transaction");
            case MIRROR -> new Root("response", "a call of a method");
            default -> new Root("response", "a " + kind.name().toLowerCase(Locale.ROOT));
        };
    }

    private static Type declared(final TypeDefinition type) {
        return new Type.DeclaredType(type.name(), List.of());
    }

    /**
     * Resolves a result: its value path starts with {@code root} and leads from {@code start} through attributes, or
     * - with {@code root} {@code null}, inside a list of result objects - starts at an attribute of the element.
     */
    private Optional<Result> result(final Binding.Result value, final @Nullable Root root, final Type start) {
        return walk(value.value(), root, start, value.location()).flatMap(walk -> {
            final Collection collection = collection(walk.type());
            final Type valueType = collection == Collection.NONE ? walk.type()
                    : ((Type.BasicType) walk.type()).arguments().getFirst();
            return converter(value.converter(), false, valueType, value.location())
                    .map(c -> new Result(value.name(), walk.fields(), c, collection));
        });
    }

    /** Resolves a list of result objects and the results of its elements. */
    private Optional<ResultList> resultList(final Binding.ResultList list, final @Nullable Root root,
                                            final Type start) {
        final Optional<Walk> walk = walk(list.value(), root, start, list.location());
        if (walk.isEmpty()) {
            return Optional.empty();
        }
        if (collection(walk.get().type()) == Collection.NONE
                || !(((Type.BasicType) walk.get().type()).arguments().getFirst() instanceof Type.DeclaredType element)) {
            error("tck.result", "'" + list.value() + "' is no list of a declared type; 'each' needs one",
                    list.location());
            return Optional.empty();
        }
        final TypeDefinition elementType = model.definition(element);
        final List<Member> members = new ArrayList<>();
        final Set<String> results = new HashSet<>();
        for (final Binding.Member member : list.members()) {
            switch (member) {
                case Binding.Result value -> {
                    if (!results.add(value.name().toString())) {
                        error("tck.duplicate", "Result '" + value.name() + "' is bound twice", value.location());
                    } else {
                        result(value, null, element).ifPresent(members::add);
                    }
                }
                case Binding.ResultList inner -> {
                    if (!results.add(inner.name().toString())) {
                        error("tck.duplicate", "Result '" + inner.name() + "' is bound twice", inner.location());
                    } else {
                        resultList(inner, null, element).ifPresent(members::add);
                    }
                }
                case Binding.Unsupported unsupported when unsupported.result() ->
                        members.add(new Unsupported(unsupported));
                default -> error("tck.syntax", "A list of result objects has only results", member.location());
            }
        }
        return Optional.of(new ResultList(list.name(), walk.get().fields(), elementType, members));
    }

    /** The attributes along a value path and the type it leads to. */
    private record Walk(List<FieldDefinition> fields, Type type) {
    }

    private Optional<Walk> walk(final Binding.Path path, final @Nullable Root root, final Type start,
                                final SourceLocation location) {
        final List<String> segments;
        if (root == null) {
            if (path.parent()) {
                error("tck.result", "'" + path + "' cannot refer to an enclosing element", location);
                return Optional.empty();
            }
            segments = path.segments();
        } else {
            if (!path.first().equals(root.name()) || path.parent()) {
                error("tck.result", "A result of " + root.owner() + " starts with '" + root.name() + "', not '"
                        + path + "'", location);
                return Optional.empty();
            }
            segments = path.segments().subList(1, path.segments().size());
        }
        Type type = start;
        TypeDefinition current = start instanceof Type.DeclaredType declared ? model.definition(declared) : null;
        final List<FieldDefinition> fields = new ArrayList<>();
        for (final String segment : segments) {
            if (current == null) {
                error("tck.result", "'" + path + "' continues after a value that has no attributes", location);
                return Optional.empty();
            }
            final Optional<FieldDefinition> field = current.field(segment);
            if (field.isEmpty()) {
                error("tck.unknown-attribute", "Type '" + current.name() + "' has no attribute '" + segment + "'",
                        location);
                return Optional.empty();
            }
            fields.add(field.get());
            type = field.get().type();
            current = type instanceof Type.DeclaredType declared ? model.definition(declared) : null;
        }
        return Optional.of(new Walk(fields, type));
    }

    private Optional<Converter> converter(final String name, final boolean inbound, final Type type,
                                          final SourceLocation location) {
        final Optional<Converter> converter = Converter.of(name);
        if (converter.isEmpty()) {
            error("tck.converter", "Unknown converter '" + name + "'", location);
            return Optional.empty();
        }
        if (inbound ? !converter.get().inbound() : !converter.get().outbound()) {
            error("tck.converter", "Converter '" + name + "' cannot convert " + (inbound ? "JSON into a value"
                    : "a value into JSON"), location);
            return Optional.empty();
        }
        if (!converter.get().accepts(type)) {
            error("tck.converter", "Converter '" + name + "' is for " + converter.get().typeNames() + ", not for '"
                    + type.text() + "'", location);
            return Optional.empty();
        }
        return converter;
    }

    private void error(final String rule, final String message, final SourceLocation location) {
        diagnostics.add(new Diagnostic(Severity.ERROR, rule, message, location));
    }
}
