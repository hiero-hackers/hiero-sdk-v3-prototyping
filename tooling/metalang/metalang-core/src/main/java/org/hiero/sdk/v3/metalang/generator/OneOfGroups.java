package org.hiero.sdk.v3.metalang.generator;

import java.util.List;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;

/**
 * The groups of {@code @@oneOf} (exactly one attribute is set) and {@code @@oneOrNoneOf} (at most one is set) of a
 * type that its constructor checks, in every language. Only groups of {@code @@immutable} attributes are checked:
 * they hold for the whole life of a value. The attributes of a mutable group are all {@code null} after construction
 * and are set one by one, so such a group can only hold when the object is used, not when it is created. A violation
 * is the standard error {@code invalid-argument-error}.
 */
public final class OneOfGroups {

    private OneOfGroups() {
    }

    /**
     * A group of attributes.
     *
     * @param exactlyOne whether exactly one is set ({@code @@oneOf}); otherwise at most one ({@code @@oneOrNoneOf})
     * @param fields     the attribute names, in the order of the annotation
     */
    public record Group(boolean exactlyOne, List<String> fields) {

        /**
         * Creates a group.
         *
         * @param exactlyOne whether exactly one is set
         * @param fields     the attribute names
         */
        public Group {
            fields = List.copyOf(fields);
        }

        /**
         * The message of a violation.
         *
         * @return e.g. {@code exactly one of a, b must be set}
         */
        public String message() {
            final String names = String.join(", ", fields);
            return exactlyOne ? "exactly one of " + names + " must be set" : "at most one of " + names + " may be set";
        }
    }

    /**
     * The groups the constructor of a type checks.
     *
     * @param type the type
     * @return the groups of immutable attributes, in the order of the annotations
     */
    public static List<Group> checked(final TypeDefinition type) {
        return type.annotations().stream()
                .filter(a -> a.name().equals("oneOf") || a.name().equals("oneOrNoneOf"))
                .map(a -> new Group(a.name().equals("oneOf"), a.arguments().stream().map(Literal::text).toList()))
                .filter(g -> g.fields().stream().allMatch(f -> type.fields().stream()
                        .anyMatch(d -> d.name().equals(f) && d.hasAnnotation("immutable"))))
                .toList();
    }
}
