package org.hiero.sdk.v3.metalang.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A resolved type declaration (complex type, abstraction or enum).
 *
 * <p>Members come in two flavours: {@code declared...} as written in the spec, and the <em>effective</em> members
 * ({@link #fields()}, {@link #methods()}) that include inherited members with the type arguments of the supertypes
 * substituted. Effective members are ordered: inherited members first (in the order of the {@code extends} clause,
 * recursively), then the type's own members; a member that overrides an inherited one (same field name, or same
 * method signature) takes the inherited member's position. {@code @@static} methods are not inherited.
 */
public sealed interface TypeDefinition extends Annotated {

    /**
     * Returns the qualified name.
     *
     * @return the name
     */
    QualifiedName name();

    /**
     * Returns the type parameters.
     *
     * @return the type parameters
     */
    List<TypeParameterDefinition> typeParameters();

    /**
     * Returns the resolved direct supertypes.
     *
     * @return the supertypes
     */
    List<Type> supertypes();

    /**
     * Returns the methods declared by this type.
     *
     * @return the declared methods
     */
    List<MethodDefinition> declaredMethods();

    /**
     * Returns the effective methods (own and inherited, substituted).
     *
     * @return the effective methods
     */
    List<MethodDefinition> methods();

    /**
     * Returns the effective attributes. For enums these are the attributes of the attribute list.
     *
     * @return the effective attributes
     */
    List<FieldDefinition> fields();

    /**
     * Returns the documentation.
     *
     * @return the documentation
     */
    String documentation();

    /**
     * Returns the effective attribute with the given name.
     *
     * @param fieldName the attribute name
     * @return the attribute, if present
     */
    default Optional<FieldDefinition> field(final String fieldName) {
        return fields().stream().filter(f -> f.name().equals(fieldName)).findFirst();
    }

    /**
     * Returns the effective methods with the given name (all overloads).
     *
     * @param methodName the method name
     * @return the methods in effective order
     */
    default List<MethodDefinition> methods(final String methodName) {
        return methods().stream().filter(m -> m.name().equals(methodName)).toList();
    }

    /**
     * Returns this type as a {@link Type}, using its own type variables as arguments.
     *
     * @return the type
     */
    default Type.DeclaredType asType() {
        return new Type.DeclaredType(name(), typeParameters().stream().map(p -> (Type) p.variable()).toList());
    }

    /**
     * A complex type or abstraction.
     *
     * @param name           the qualified name
     * @param abstraction    whether declared with {@code abstraction}
     * @param typeParameters the type parameters
     * @param supertypes     the resolved supertypes
     * @param declaredFields the attributes declared by this type
     * @param declaredMethods the methods declared by this type
     * @param fields         the effective attributes
     * @param methods        the effective methods
     * @param annotations    the annotations
     * @param documentation  the documentation
     * @param location       the source location
     */
    record ComplexTypeDefinition(QualifiedName name, boolean abstraction, List<TypeParameterDefinition> typeParameters,
                                 List<Type> supertypes, List<FieldDefinition> declaredFields,
                                 List<MethodDefinition> declaredMethods, List<FieldDefinition> fields,
                                 List<MethodDefinition> methods, List<Annotation> annotations, String documentation,
                                 SourceLocation location) implements TypeDefinition {
        /**
         * Creates a complex type definition.
         *
         * @param name            the name
         * @param abstraction     whether it is an abstraction
         * @param typeParameters  the type parameters
         * @param supertypes      the supertypes
         * @param declaredFields  the declared attributes
         * @param declaredMethods the declared methods
         * @param fields          the effective attributes
         * @param methods         the effective methods
         * @param annotations     the annotations
         * @param documentation   the documentation
         * @param location        the source location
         */
        public ComplexTypeDefinition {
            Objects.requireNonNull(name, "name must not be null");
            typeParameters = List.copyOf(typeParameters);
            supertypes = List.copyOf(supertypes);
            declaredFields = List.copyOf(declaredFields);
            declaredMethods = List.copyOf(declaredMethods);
            fields = List.copyOf(fields);
            methods = List.copyOf(methods);
            annotations = List.copyOf(annotations);
            Objects.requireNonNull(documentation, "documentation must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * An enumeration.
     *
     * @param name            the qualified name
     * @param attributes      the attribute list ({@code enum Name(attr: Type)})
     * @param values          the values with their attribute values
     * @param supertypes      the resolved supertypes
     * @param declaredMethods the methods declared by the enum
     * @param methods         the effective methods
     * @param annotations     the annotations
     * @param documentation   the documentation
     * @param location        the source location
     */
    record EnumDefinition(QualifiedName name, List<ParameterDefinition> attributes, List<EnumValueDefinition> values,
                          List<Type> supertypes, List<MethodDefinition> declaredMethods, List<MethodDefinition> methods,
                          List<Annotation> annotations, String documentation, SourceLocation location)
            implements TypeDefinition {
        /**
         * Creates an enum definition.
         *
         * @param name            the name
         * @param attributes      the attributes
         * @param values          the values
         * @param supertypes      the supertypes
         * @param declaredMethods the declared methods
         * @param methods         the effective methods
         * @param annotations     the annotations
         * @param documentation   the documentation
         * @param location        the source location
         */
        public EnumDefinition {
            Objects.requireNonNull(name, "name must not be null");
            attributes = List.copyOf(attributes);
            values = List.copyOf(values);
            supertypes = List.copyOf(supertypes);
            declaredMethods = List.copyOf(declaredMethods);
            methods = List.copyOf(methods);
            annotations = List.copyOf(annotations);
            Objects.requireNonNull(documentation, "documentation must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }

        @Override
        public List<TypeParameterDefinition> typeParameters() {
            return List.of();
        }

        @Override
        public List<FieldDefinition> fields() {
            return attributes.stream().map(a -> new FieldDefinition(a.name(), a.type(), name, a.annotations(), "",
                    a.location())).toList();
        }
    }
}
