package org.hiero.sdk.v3.metalang.tck;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

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
 * @param common        whether this is the binding of the common transaction parameters ({@code common}) instead of
 *                      a method ({@code binding})
 * @param name          the JSON-RPC method, or the JSON object of the common parameters
 * @param type          the name of the target type as written (simple or qualified)
 * @param members       the assignments, results and unsupported parameters
 * @param documentation the comment above the binding
 * @param location      where the binding starts
 */
public record Binding(boolean common, String name, String type, List<Member> members, String documentation,
                      SourceLocation location) {

    /**
     * Creates a binding.
     *
     * @param common        whether it binds the common transaction parameters
     * @param name          the method or JSON object
     * @param type          the target type
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
     * The first source that is present in the JSON request is used.
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
     * A list attribute built from the elements of a JSON list:
     * {@code attribute = each list.object -> Type { ... }}. For every element of {@code list} that has the object
     * {@code object}, an instance of {@code Type} is created from that object; {@code ^name} refers to the element.
     *
     * @param attribute the list attribute of the target type
     * @param source    the JSON list and the object of each element
     * @param type      the element type as written
     * @param members   the assignments of the element type
     * @param location  the location
     */
    public record Each(String attribute, Path source, String type, List<Member> members, SourceLocation location)
            implements Member {

        /**
         * Creates a list mapping.
         *
         * @param attribute the attribute
         * @param source    the JSON list path
         * @param type      the element type
         * @param members   the members
         * @param location  the location
         */
        public Each {
            Objects.requireNonNull(attribute, "attribute must not be null");
            Objects.requireNonNull(source, "source must not be null");
            Objects.requireNonNull(type, "type must not be null");
            members = List.copyOf(members);
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * A field of the JSON result: {@code result name = receipt.path : converter}. The name may be a path
     * ({@code stakingInfo.stakedNodeId}) for nested result objects.
     *
     * @param name      the result field
     * @param value     the path to the value, starting with {@code receipt} or {@code response}
     * @param converter the converter from the API value to JSON
     * @param location  the location
     */
    public record Result(Path name, Path value, String converter, SourceLocation location) implements Member {

        /**
         * Creates a result.
         *
         * @param name      the result field
         * @param value     the value path
         * @param converter the converter
         * @param location  the location
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
     * {@code unsupported result name "reason"}. Requests that use it are answered with an error.
     *
     * @param result    whether it is a result field
     * @param name      the parameter or result field
     * @param reason    why the API cannot provide it
     * @param location  the location
     */
    public record Unsupported(boolean result, String name, String reason, SourceLocation location)
            implements Member {

        /**
         * Creates an unsupported parameter.
         *
         * @param result   whether it is a result field
         * @param name     the name
         * @param reason   the reason
         * @param location the location
         */
        public Unsupported {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(reason, "reason must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * A JSON source of an attribute: {@code path : converter}.
     *
     * @param path      the JSON path
     * @param converter the converter from JSON to the API value
     */
    public record Source(Path path, String converter) {

        /**
         * Creates a source.
         *
         * @param path      the path
         * @param converter the converter
         */
        public Source {
            Objects.requireNonNull(path, "path must not be null");
            Objects.requireNonNull(converter, "converter must not be null");
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
