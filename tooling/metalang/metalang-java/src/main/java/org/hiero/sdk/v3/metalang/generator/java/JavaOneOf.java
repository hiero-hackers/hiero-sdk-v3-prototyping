package org.hiero.sdk.v3.metalang.generator.java;

import java.util.List;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;

/**
 * The groups of {@code @@oneOf} (exactly one attribute is set) and {@code @@oneOrNoneOf} (at most one is set) of a
 * type, checked by its constructor. Only groups of {@code @@immutable} attributes are checked: they hold for the whole
 * life of a value. The attributes of a mutable group are all {@code null} after construction and are set one by one,
 * so such a group can only hold when the object is used, not when it is created.
 */
final class JavaOneOf {

    private JavaOneOf() {
    }

    /**
     * A group of attributes.
     *
     * @param exactlyOne whether exactly one is set ({@code @@oneOf}); otherwise at most one ({@code @@oneOrNoneOf})
     * @param fields     the attribute names, in the order of the annotation
     */
    record Group(boolean exactlyOne, List<String> fields) {
    }

    /**
     * The groups the constructor of a type checks.
     *
     * @param type the type
     * @return the groups of immutable attributes, in the order of the annotations
     */
    static List<Group> checked(final TypeDefinition type) {
        return type.annotations().stream()
                .filter(a -> a.name().equals("oneOf") || a.name().equals("oneOrNoneOf"))
                .map(a -> new Group(a.name().equals("oneOf"), a.arguments().stream().map(Literal::text).toList()))
                .filter(g -> g.fields().stream().allMatch(f -> type.fields().stream()
                        .anyMatch(d -> d.name().equals(f) && d.hasAnnotation("immutable"))))
                .toList();
    }

    /**
     * The statements that check the groups in a constructor.
     *
     * @param type   the type
     * @param indent the indentation of the statements
     * @return the statements, empty if the type has no checked group
     */
    static String checks(final TypeDefinition type, final String indent) {
        final StringBuilder java = new StringBuilder();
        for (final Group group : checked(type)) {
            final String count = group.fields().stream()
                    .map(f -> "(" + JavaKeywords.identifier(f) + " != null ? 1 : 0)")
                    .collect(Collectors.joining(" + "));
            final String names = String.join(", ", group.fields());
            java.append(indent).append("if (").append(count).append(group.exactlyOne() ? " != 1" : " > 1")
                    .append(") {\n").append(indent).append("    throw new IllegalArgumentException(\"")
                    .append(group.exactlyOne() ? "exactly one of " + names + " must be set"
                            : "at most one of " + names + " may be set").append("\");\n").append(indent).append("}\n");
        }
        return java.toString();
    }

    /**
     * The group an attribute belongs to.
     *
     * @param type  the type
     * @param field the attribute
     * @return the group, {@code null} if the attribute is in no checked group
     */
    static Group groupOf(final TypeDefinition type, final FieldDefinition field) {
        return checked(type).stream().filter(g -> g.fields().contains(field.name())).findFirst().orElse(null);
    }
}
