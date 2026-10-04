package org.hiero.sdk.v3.metalang.tck;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
 * ({@code queryReceipt}); a query is sent with {@code submit} and answered with the value of its response.
 */
public final class TckBindings {

    /** The kind of a binding, derived from its target type. */
    public enum Kind {
        /** a transaction: answered with its receipt */
        TRANSACTION,
        /** a query: answered with its response */
        QUERY,
        /** the common transaction parameters: setters of mutable attributes */
        COMMON
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
     *                   common parameters
     */
    public record Resolved(Binding binding, String file, Kind kind, TypeDefinition type, List<Member> members,
                           @Nullable TypeDefinition resultType) {

        /**
         * Creates a resolved binding.
         *
         * @param binding    the binding
         * @param file       the file
         * @param kind       the kind
         * @param type       the target type
         * @param members    the members
         * @param resultType the result type
         */
        public Resolved {
            Objects.requireNonNull(binding, "binding must not be null");
            members = List.copyOf(members);
        }
    }

    /** A resolved member of a binding. */
    public sealed interface Member {
    }

    /**
     * An attribute with its JSON sources.
     *
     * @param field   the attribute
     * @param sources the alternative sources with their converters
     */
    public record Value(FieldDefinition field, List<Source> sources) implements Member {

        /**
         * Creates a value.
         *
         * @param field   the attribute
         * @param sources the sources
         */
        public Value {
            sources = List.copyOf(sources);
        }
    }

    /**
     * A JSON source with its converter.
     *
     * @param path      the JSON path
     * @param converter the converter
     */
    public record Source(Binding.Path path, Converter converter) {
    }

    /**
     * A list attribute built from the elements of a JSON list.
     *
     * @param field       the list attribute
     * @param source      the JSON list and the object of each element
     * @param elementType the element type
     * @param members     the values of the element type
     */
    public record Elements(FieldDefinition field, Binding.Path source, TypeDefinition elementType,
                           List<Member> members) implements Member {

        /**
         * Creates a list mapping.
         *
         * @param field       the attribute
         * @param source      the source
         * @param elementType the element type
         * @param members     the members
         */
        public Elements {
            members = List.copyOf(members);
        }
    }

    /**
     * A field of the JSON result.
     *
     * @param name      the result field (a path for nested objects)
     * @param fields    the attributes from the receipt or response to the value
     * @param converter the converter to JSON
     */
    public record Result(Binding.Path name, List<FieldDefinition> fields, Converter converter) implements Member {

        /**
         * Creates a result.
         *
         * @param name      the result field
         * @param fields    the attribute path
         * @param converter the converter
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
            for (final Binding binding : parsed.bindings()) {
                final Optional<Resolved> resolved = resolver.binding(binding, file.getKey());
                if (resolved.isEmpty()) {
                    continue;
                }
                if (binding.common()) {
                    if (common != null) {
                        resolver.error("tck.duplicate", "The common transaction parameters are bound twice",
                                binding.location());
                    }
                    common = resolved.get();
                } else if (!methods.add(binding.name())) {
                    resolver.error("tck.duplicate", "Method '" + binding.name() + "' is bound twice",
                            binding.location());
                } else {
                    bindings.add(resolved.get());
                }
            }
        }
        return new Bindings(bindings, Optional.ofNullable(common), resolver.diagnostics);
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
        final Kind kind;
        final TypeDefinition resultType;
        if (binding.common()) {
            kind = Kind.COMMON;
            resultType = null;
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
        final List<Member> members = members(binding.members(), type.get(), kind, resultType, false);
        if (kind == Kind.TRANSACTION || kind == Kind.QUERY) {
            required(binding.members(), type.get(), binding.location());
        }
        return Optional.of(new Resolved(binding, file, kind, type.get(), members, resultType));
    }

    /** The receipt or response type: the first type argument of the result of the method that sends the request. */
    private Optional<TypeDefinition> resultOf(final TypeDefinition type, final String method) {
        return type.methods(method).stream().filter(m -> !m.isStatic()).map(MethodDefinition::returnType)
                .filter(Type.DeclaredType.class::isInstance).map(Type.DeclaredType.class::cast)
                .filter(t -> !t.arguments().isEmpty() && t.arguments().getFirst() instanceof Type.DeclaredType)
                .map(t -> model.definition((Type.DeclaredType) t.arguments().getFirst())).findFirst();
    }

    private List<Member> members(final List<Binding.Member> members, final TypeDefinition type, final Kind kind,
                                 final @Nullable TypeDefinition resultType, final boolean insideEach) {
        final List<Member> result = new ArrayList<>();
        final Set<String> attributes = new HashSet<>();
        final Set<String> results = new HashSet<>();
        for (final Binding.Member member : members) {
            switch (member) {
                case Binding.Assignment assignment -> attribute(type, assignment.attribute(), kind,
                        assignment.location(), attributes).ifPresent(field -> {
                    final List<Source> sources = new ArrayList<>();
                    for (final Binding.Source source : assignment.sources()) {
                        if (source.path().parent() && !insideEach) {
                            error("tck.parent", "'" + source.path() + "' refers to the enclosing element, but '"
                                    + assignment.attribute() + "' is not inside 'each'", assignment.location());
                            continue;
                        }
                        converter(source.converter(), true, field.type(), assignment.location())
                                .ifPresent(c -> sources.add(new Source(source.path(), c)));
                    }
                    result.add(new Value(field, sources));
                });
                case Binding.Each each -> attribute(type, each.attribute(), kind, each.location(), attributes)
                        .ifPresent(field -> elements(field, each).ifPresent(result::add));
                case Binding.Result value -> {
                    if (resultType == null) {
                        error("tck.result", "The common transaction parameters have no result", value.location());
                    } else if (!results.add(value.name().toString())) {
                        error("tck.duplicate", "Result '" + value.name() + "' is bound twice", value.location());
                    } else {
                        result(value, kind, resultType).ifPresent(result::add);
                    }
                }
                case Binding.Unsupported unsupported -> result.add(new Unsupported(unsupported));
            }
        }
        return result;
    }

    private Optional<FieldDefinition> attribute(final TypeDefinition type, final String name, final Kind kind,
                                                final SourceLocation location, final Set<String> assigned) {
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
        return field;
    }

    private Optional<Elements> elements(final FieldDefinition field, final Binding.Each each) {
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
        final List<Member> members = members(each.members(), type.get(), Kind.TRANSACTION, null, true).stream()
                .filter(m -> !(m instanceof Result)).toList();
        required(each.members(), type.get(), each.location());
        return Optional.of(new Elements(field, each.source(), type.get(), members));
    }

    /** Reports the required attributes (no null, no default) that get no value: requests cannot be built then. */
    private void required(final List<Binding.Member> members, final TypeDefinition type,
                          final SourceLocation location) {
        final Set<String> assigned = new HashSet<>();
        members.forEach(m -> {
            if (m instanceof Binding.Assignment a) {
                assigned.add(a.attribute());
            } else if (m instanceof Binding.Each e) {
                assigned.add(e.attribute());
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

    private Optional<Result> result(final Binding.Result value, final Kind kind, final TypeDefinition resultType) {
        final String root = kind == Kind.TRANSACTION ? "receipt" : "response";
        if (!value.value().first().equals(root) || value.value().parent() || value.value().segments().size() < 2) {
            error("tck.result", "A result of a " + kind.name().toLowerCase(java.util.Locale.ROOT) + " starts with '"
                    + root + ".', not '" + value.value() + "'", value.location());
            return Optional.empty();
        }
        TypeDefinition current = resultType;
        final List<FieldDefinition> fields = new ArrayList<>();
        for (final String segment : value.value().rest().segments()) {
            if (current == null) {
                error("tck.result", "'" + value.value() + "' continues after a value that has no attributes",
                        value.location());
                return Optional.empty();
            }
            final Optional<FieldDefinition> field = current.field(segment);
            if (field.isEmpty()) {
                error("tck.unknown-attribute", "Type '" + current.name() + "' has no attribute '" + segment + "'",
                        value.location());
                return Optional.empty();
            }
            fields.add(field.get());
            current = field.get().type() instanceof Type.DeclaredType declared ? model.definition(declared) : null;
        }
        return converter(value.converter(), false, fields.getLast().type(), value.location())
                .map(c -> new Result(value.name(), fields, c));
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
