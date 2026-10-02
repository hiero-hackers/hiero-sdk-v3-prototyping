package org.hiero.sdk.v3.metalang.ast;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A top-level declaration of a namespace.
 */
public sealed interface Declaration extends Annotated {

    /**
     * Returns the declared name.
     *
     * @return the name
     */
    String name();

    /**
     * Returns the attached documentation comment (may be empty).
     *
     * @return the documentation
     */
    String documentation();

    /**
     * A type that can be referenced by name (complex type or enum).
     */
    sealed interface TypeDeclaration extends Declaration {

        /**
         * Returns the declared super types ({@code extends ...}).
         *
         * @return the super types
         */
        List<TypeRef> supertypes();

        /**
         * Returns the declared fields.
         *
         * @return the fields
         */
        List<Field> fields();

        /**
         * Returns the declared methods.
         *
         * @return the methods
         */
        List<Method> methods();

        /**
         * Returns the declared generic type parameters.
         *
         * @return the type parameters
         */
        List<TypeParameter> typeParameters();
    }

    /**
     * A complex type, optionally abstract ({@code abstraction}).
     *
     * @param name           the type name
     * @param annotations    the type annotations
     * @param documentation  the documentation
     * @param abstraction    whether declared with {@code abstraction}
     * @param typeKeyword    whether declared with the non-standard {@code type} keyword
     * @param typeParameters the generic type parameters
     * @param supertypes     the super types
     * @param fields         the fields
     * @param methods        the methods
     * @param location       the source location
     */
    record ComplexType(String name, List<Annotation> annotations, String documentation, boolean abstraction,
                       boolean typeKeyword, List<TypeParameter> typeParameters, List<TypeRef> supertypes,
                       List<Field> fields, List<Method> methods, SourceLocation location)
            implements TypeDeclaration {
        /**
         * Creates a complex type.
         *
         * @param name           the name
         * @param annotations    the annotations
         * @param documentation  the documentation
         * @param abstraction    whether it is abstract
         * @param typeKeyword    whether the {@code type} keyword was used
         * @param typeParameters the type parameters
         * @param supertypes     the super types
         * @param fields         the fields
         * @param methods        the methods
         * @param location       the source location
         */
        public ComplexType {
            Objects.requireNonNull(name, "name must not be null");
            annotations = List.copyOf(Objects.requireNonNull(annotations, "annotations must not be null"));
            Objects.requireNonNull(documentation, "documentation must not be null");
            typeParameters = List.copyOf(Objects.requireNonNull(typeParameters, "typeParameters must not be null"));
            supertypes = List.copyOf(Objects.requireNonNull(supertypes, "supertypes must not be null"));
            fields = List.copyOf(Objects.requireNonNull(fields, "fields must not be null"));
            methods = List.copyOf(Objects.requireNonNull(methods, "methods must not be null"));
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * An enumeration.
     *
     * @param name          the enum name
     * @param annotations   the annotations
     * @param documentation the documentation
     * @param supertypes    the super types
     * @param values        the enum values
     * @param fields        the fields
     * @param methods       the methods
     * @param location      the source location
     */
    record EnumType(String name, List<Annotation> annotations, String documentation, List<TypeRef> supertypes,
                    List<EnumValue> values, List<Field> fields,
                    List<Method> methods, SourceLocation location) implements TypeDeclaration {
        /**
         * Creates an enum.
         *
         * @param name          the name
         * @param annotations   the annotations
         * @param documentation the documentation
         * @param supertypes    the super types
         * @param values        the values
         * @param fields        the fields
         * @param methods       the methods
         * @param location      the source location
         */
        public EnumType {
            Objects.requireNonNull(name, "name must not be null");
            annotations = List.copyOf(Objects.requireNonNull(annotations, "annotations must not be null"));
            Objects.requireNonNull(documentation, "documentation must not be null");
            supertypes = List.copyOf(Objects.requireNonNull(supertypes, "supertypes must not be null"));
            values = List.copyOf(Objects.requireNonNull(values, "values must not be null"));
            fields = List.copyOf(Objects.requireNonNull(fields, "fields must not be null"));
            methods = List.copyOf(Objects.requireNonNull(methods, "methods must not be null"));
            Objects.requireNonNull(location, "location must not be null");
        }

        @Override
        public List<TypeParameter> typeParameters() {
            return List.of();
        }
    }

    /**
     * A namespace-level constant.
     *
     * @param name          the constant name
     * @param annotations   the annotations
     * @param documentation the documentation
     * @param type          the declared type
     * @param value         the value
     * @param location      the source location
     */
    record Constant(String name, List<Annotation> annotations, String documentation, TypeRef type, Literal value,
                    SourceLocation location) implements Declaration {
        /**
         * Creates a constant.
         *
         * @param name          the name
         * @param annotations   the annotations
         * @param documentation the documentation
         * @param type          the type
         * @param value         the value
         * @param location      the source location
         */
        public Constant {
            Objects.requireNonNull(name, "name must not be null");
            annotations = List.copyOf(Objects.requireNonNull(annotations, "annotations must not be null"));
            Objects.requireNonNull(documentation, "documentation must not be null");
            Objects.requireNonNull(type, "type must not be null");
            Objects.requireNonNull(value, "value must not be null");
            Objects.requireNonNull(location, "location must not be null");
        }
    }

    /**
     * A namespace-level function (must be {@code @@static}, see guideline section "Namespace-level
     * functions").
     *
     * @param method the function signature
     */
    record Function(Method method) implements Declaration {
        /**
         * Creates a function.
         *
         * @param method the signature
         */
        public Function {
            Objects.requireNonNull(method, "method must not be null");
        }

        @Override
        public String name() {
            return method.name();
        }

        @Override
        public List<Annotation> annotations() {
            return method.annotations();
        }

        @Override
        public String documentation() {
            return method.documentation();
        }

        @Override
        public SourceLocation location() {
            return method.location();
        }
    }
}
