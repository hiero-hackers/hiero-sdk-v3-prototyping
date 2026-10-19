package org.hiero.sdk.v3.metalang.tck;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.jspecify.annotations.Nullable;

/**
 * The binding of a TCK JSON-RPC method (or of the common transaction parameters) to a type of the API, as written in
 * a bindings file: which type is created, which JSON parameter goes to which attribute with which converter, and
 * which fields form the result.
 *
 * <pre>
 * // Creates an account.
 * binding createAccount -&gt; AccountCreateTransaction {
 *     authority      = key : key
 *     initialBalance = initialBalance : tinybar
 *     result accountId = receipt.accountId : accountId
 *     unsupported alias "reason"
 * }
 * </pre>
 *
 * <p>A method that maps to one of several types depending on its parameters is written with variants
 * ({@code binding mintToken when metadata -> A { ... } else -> B { ... }}); every variant is a binding of its own with
 * the same name, the condition in {@link #when()} and the documentation of the first one.
 *
 * @param common        whether this is the binding of the common transaction parameters ({@code common}) instead of
 *                      a method ({@code binding})
 * @param name          the JSON-RPC method, or the JSON object of the common parameters
 * @param type          the name of the target type as written (simple or qualified)
 * @param when          the JSON parameter whose presence selects this variant, {@code null} for the only binding of a
 *                      method or for the {@code else} variant
 * @param members       the assignments, results and unsupported parameters
 * @param documentation the comment above the binding
 * @param location      where the binding (or the variant) starts
 */
public record Binding(boolean common, String name, String type, @Nullable Path when, List<Member> members,
                      String documentation, SourceLocation location) {

    /**
     * Creates a binding.
     *
     * @param common        whether it binds the common transaction parameters
     * @param name          the method or JSON object
     * @param type          the target type
     * @param when          the condition of the variant, {@code null} if none
     * @param members       the members
     * @param documentation the documentation
     * @param location      the location
     */
    public Binding {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        members = List.copyOf(members);
        Objects.requireNonNull(documentation, "documentation must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }

    /** A member of a binding. */
    public sealed interface Member {

        /**
         * Returns where the member is written.
         *
         * @return the location
         */
        SourceLocation location();
    }

    /**
     * An attribute that gets its value from JSON parameters: {@code attribute = path : converter | path : converter}.
     * The first source that is present in the JSON request is used. For a {@code list} or {@code set} attribute the
     * converter of the element type converts every element of a JSON list.
     *
     * @param attribute the attribute of the target type
     * @param sources   the alternative sources
     * @param location  the location
     */
    public record Assignment(String attribute, List<Source> sources, SourceLocation location) implements Member {

        /**
         * Creates an assignment.
         *
         * @param attribute the attribute
         * @param sources   the sources
         * @param location  the location
         */
        public Assignment {
            Objects.requireNonNull(attribute, "attribute must not be null");
            sources = List.copyOf(sources);
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * A list attribute built from JSON lists: {@code attribute = each generator, generator -> Type { ... }}. The
     * generators iterate like nested loops; for every combination an instance of {@code Type} is created.
     *
     * @param attribute  the list attribute of the target type
     * @param generators the iterations, at least one
     * @param type       the element type as written
     * @param members    the assignments of the element type
     * @param location   the location
     */
    public record Each(String attribute, List<Generator> generators, String type, List<Member> members,
                       SourceLocation location) implements Member {

        /**
         * Creates a list mapping.
         *
         * @param attribute  the attribute
         * @param generators the iterations
         * @param type       the element type
         * @param members    the members
         * @param location   the location
         */
        public Each {
            Objects.requireNonNull(attribute, "attribute must not be null");
            generators = List.copyOf(generators);
            if (generators.isEmpty()) {
                throw new IllegalArgumentException("'each' needs at least one generator");
            }
            Objects.requireNonNull(type, "type must not be null");
            members = List.copyOf(members);
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * An iteration of {@code each}.
     * <ul>
     *   <li>{@code list.object} (no name): every element of the JSON list {@code list} that has the object
     *       {@code object} (or every element, without {@code .object}) becomes the JSON object the members read;
     *       {@code ^name} refers to the element.</li>
     *   <li>{@code name in list}: every element of the JSON list {@code list} (a scalar) is bound to {@code name},
     *       which the members read like a parameter; the JSON object stays the same. With {@code ?} an absent list
     *       counts as one iteration without the name.</li>
     * </ul>
     *
     * @param name     the name of a scalar element, {@code null} for objects
     * @param path     the JSON list (and, for objects, the object in each element)
     * @param optional whether an absent list of scalars counts as one iteration ({@code ?})
     */
    public record Generator(@Nullable String name, Path path, boolean optional) {

        /**
         * Creates a generator.
         *
         * @param name     the name of a scalar element
         * @param path     the path
         * @param optional whether an absent list counts as one iteration
         */
        public Generator {
            Objects.requireNonNull(path, "path must not be null");
        }
    }

    /**
     * An attribute of a declared type created from the JSON request:
     * {@code attribute = path -> Type { ... }} reads the JSON object {@code path}, {@code attribute = -> Type { ... }}
     * the current one; with variants ({@code attribute = when a -> A { ... } else -> B { ... }}) the type follows from
     * the presence of a JSON parameter.
     *
     * @param attribute the attribute of the target type
     * @param path      the JSON object to read, {@code null} for the current one
     * @param variants  the types, at least one
     * @param location  the location
     */
    public record Construct(String attribute, @Nullable Path path, List<Variant> variants, SourceLocation location)
            implements Member {

        /**
         * Creates a construction.
         *
         * @param attribute the attribute
         * @param path      the JSON object
         * @param variants  the variants
         * @param location  the location
         */
        public Construct {
            Objects.requireNonNull(attribute, "attribute must not be null");
            variants = List.copyOf(variants);
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * A type with its members, selected by the presence of a JSON parameter.
     *
     * @param when     the JSON parameter, {@code null} for the only or the {@code else} variant
     * @param type     the type as written
     * @param members  the members
     * @param location the location
     */
    public record Variant(@Nullable Path when, String type, List<Member> members, SourceLocation location) {

        /**
         * Creates a variant.
         *
         * @param when     the condition
         * @param type     the type
         * @param members  the members
         * @param location the location
         */
        public Variant {
            Objects.requireNonNull(type, "type must not be null");
            members = List.copyOf(members);
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * A transaction attribute built from a nested TCK request: {@code attribute = request path}. The JSON object at
     * {@code path} holds {@code method} and {@code params}; the transaction of that method's binding is built without
     * being sent (e.g. the scheduled transaction of a schedule).
     *
     * @param attribute the attribute of the target type
     * @param path      the JSON object of the request
     * @param location  the location
     */
    public record Request(String attribute, Path path, SourceLocation location) implements Member {

        /**
         * Creates a nested request.
         *
         * @param attribute the attribute
         * @param path      the path
         * @param location  the location
         */
        public Request {
            Objects.requireNonNull(attribute, "attribute must not be null");
            Objects.requireNonNull(path, "path must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * A field of the JSON result: {@code result name = receipt.path : converter}. The name may be a path
     * ({@code stakingInfo.stakedNodeId}) for nested result objects; the value {@code receipt} or {@code response}
     * alone is the whole receipt or response.
     *
     * @param name      the result field
     * @param value     the path to the value, starting with {@code receipt} or {@code response}
     * @param converter    the converter from the API value to JSON
     * @param undocumented whether the TCK tests read the result although the test specification of the method does
     *                     not name it ({@code undocumented result ...})
     * @param location     the location
     */
    public record Result(Path name, Path value, String converter, boolean undocumented, SourceLocation location)
            implements Member {

        /**
         * Creates a result the test specification names.
         *
         * @param name      the result field
         * @param value     the value path
         * @param converter the converter
         * @param location  the location
         */
        public Result(final Path name, final Path value, final String converter, final SourceLocation location) {
            this(name, value, converter, false, location);
        }

        /**
         * Creates a result.
         *
         * @param name         the result field
         * @param value        the value path
         * @param converter    the converter
         * @param undocumented whether the test specification does not name it
         * @param location     the location
         */
        public Result {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(value, "value must not be null");
            Objects.requireNonNull(converter, "converter must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * A TCK parameter or result field that the API cannot provide: {@code unsupported name "reason"} or
     * {@code unsupported result name "reason"}. Requests that use it are answered with an error - except a parameter
     * with the value of {@code unless} ({@code unsupported name "reason" unless false}): the API behaves that way
     * anyway.
     *
     * @param result    whether it is a result field
     * @param name      the parameter or result field
     * @param reason    why the API cannot provide it
     * @param unless    the JSON value of the parameter that is accepted (a {@code Boolean} or a {@code String}),
     *                  {@code null} if none is
     * @param location  the location
     */
    public record Unsupported(boolean result, String name, String reason, @Nullable Object unless,
                              SourceLocation location) implements Member {

        /**
         * Creates an unsupported parameter or result without accepted value.
         *
         * @param result   whether it is a result field
         * @param name     the name
         * @param reason   the reason
         * @param location the location
         */
        public Unsupported(final boolean result, final String name, final String reason,
                           final SourceLocation location) {
            this(result, name, reason, null, location);
        }

        /**
         * Creates an unsupported parameter.
         *
         * @param result   whether it is a result field
         * @param name     the name
         * @param reason   the reason
         * @param unless   the accepted JSON value, or {@code null}
         * @param location the location
         */
        public Unsupported {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(reason, "reason must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * A JSON source of an attribute: {@code path : converter}, or a constant {@code "text" : converter} that is
     * converted like a JSON string (a default where the TCK has one and the API has none).
     *
     * @param path      the JSON path, {@code null} for a constant
     * @param literal   the constant, {@code null} for a JSON path
     * @param converter the converter from JSON to the API value
     */
    public record Source(@Nullable Path path, @Nullable String literal, String converter) {

        /**
         * Creates a source.
         *
         * @param path      the path
         * @param literal   the constant
         * @param converter the converter
         */
        public Source {
            if ((path == null) == (literal == null)) {
                throw new IllegalArgumentException("a source is either a path or a constant");
            }
            Objects.requireNonNull(converter, "converter must not be null");
        }

        /**
         * Creates a source of a JSON path.
         *
         * @param path      the path
         * @param converter the converter
         */
        public Source(final Path path, final String converter) {
            this(path, null, converter);
        }

        @Override
        public String toString() {
            return path != null ? path.toString() : "\"" + literal + "\"";
        }
    }

    /**
     * A path of names: {@code transfers.hbar}, {@code receipt.accountId}; {@code ^approved} refers to the enclosing
     * element of an {@code each}.
     *
     * @param parent   whether it starts at the enclosing element ({@code ^})
     * @param segments the names
     */
    public record Path(boolean parent, List<String> segments) {

        /**
         * Creates a path.
         *
         * @param parent   whether it starts at the enclosing element
         * @param segments the names, at least one
         */
        public Path {
            segments = List.copyOf(segments);
            if (segments.isEmpty()) {
                throw new IllegalArgumentException("a path needs at least one name");
            }
        }

        /** Returns the first name. */
        public String first() {
            return segments.getFirst();
        }

        /** Returns the last name. */
        public String last() {
            return segments.getLast();
        }

        /** Returns the path without its first name. */
        public Path rest() {
            return new Path(false, segments.subList(1, segments.size()));
        }

        @Override
        public String toString() {
            return (parent ? "^" : "") + String.join(".", segments);
        }
    }
}
