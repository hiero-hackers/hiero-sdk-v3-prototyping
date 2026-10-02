package org.hiero.sdk.v3.metalang.semantic;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * All annotations defined by the meta-language, with the elements they may annotate and the shape of
 * their arguments.
 */
public enum KnownAnnotation {

    IMMUTABLE("immutable", Arguments.NONE, ElementKind.FIELD),
    NULLABLE("nullable", Arguments.NONE, ElementKind.FIELD, ElementKind.PARAMETER, ElementKind.METHOD,
            ElementKind.FUNCTION),
    DEPRECATED("deprecated", Arguments.NONE, ElementKind.COMPLEX_TYPE, ElementKind.ABSTRACTION, ElementKind.ENUM,
            ElementKind.ENUM_VALUE, ElementKind.FIELD, ElementKind.METHOD, ElementKind.FUNCTION, ElementKind.CONSTANT),
    OVERRIDE("override", Arguments.NONE, ElementKind.FIELD),
    DEFAULT("default", Arguments.ONE_LITERAL, ElementKind.FIELD),
    MIN("min", Arguments.ONE_NUMBER, ElementKind.FIELD, ElementKind.PARAMETER),
    MAX("max", Arguments.ONE_NUMBER, ElementKind.FIELD, ElementKind.PARAMETER),
    MIN_LENGTH("minLength", Arguments.ONE_NON_NEGATIVE_INTEGER, ElementKind.FIELD, ElementKind.PARAMETER),
    MAX_LENGTH("maxLength", Arguments.ONE_NON_NEGATIVE_INTEGER, ElementKind.FIELD, ElementKind.PARAMETER),
    MIN_SIZE("minSize", Arguments.ONE_NON_NEGATIVE_INTEGER, ElementKind.FIELD, ElementKind.PARAMETER),
    MAX_SIZE("maxSize", Arguments.ONE_NON_NEGATIVE_INTEGER, ElementKind.FIELD, ElementKind.PARAMETER),
    PATTERN("pattern", Arguments.ONE_STRING, ElementKind.FIELD, ElementKind.PARAMETER),
    URL_PATTERN("urlPattern", Arguments.NONE, ElementKind.FIELD, ElementKind.PARAMETER),
    THREAD_SAFE("threadSafe", Arguments.OPTIONAL_NAME, ElementKind.FIELD, ElementKind.METHOD,
            ElementKind.COMPLEX_TYPE, ElementKind.ABSTRACTION, ElementKind.ENUM),
    ASYNC("async", Arguments.NONE, ElementKind.METHOD, ElementKind.FUNCTION),
    STREAMING("streaming", Arguments.NONE, ElementKind.METHOD),
    STATIC("static", Arguments.NONE, ElementKind.METHOD, ElementKind.FUNCTION),
    FINAL_METHOD("finalMethod", Arguments.NONE, ElementKind.METHOD),
    THROWS("throws", Arguments.NAMES, ElementKind.METHOD, ElementKind.FUNCTION),
    ONE_OF("oneOf", Arguments.AT_LEAST_TWO_NAMES, ElementKind.COMPLEX_TYPE, ElementKind.ABSTRACTION),
    ONE_OR_NONE_OF("oneOrNoneOf", Arguments.AT_LEAST_TWO_NAMES, ElementKind.COMPLEX_TYPE, ElementKind.ABSTRACTION),
    SEALED("sealed", Arguments.NAMES, ElementKind.COMPLEX_TYPE, ElementKind.ABSTRACTION),
    FINAL_TYPE("finalType", Arguments.NONE, ElementKind.COMPLEX_TYPE, ElementKind.ABSTRACTION);

    /**
     * Kinds of elements an annotation can be attached to.
     */
    public enum ElementKind {
        /** A concrete complex type. */
        COMPLEX_TYPE,
        /** An {@code abstraction}. */
        ABSTRACTION,
        /** An enum. */
        ENUM,
        /** An enum value. */
        ENUM_VALUE,
        /** An attribute of a complex type or enum. */
        FIELD,
        /** A method of a complex type or enum. */
        METHOD,
        /** A namespace-level function. */
        FUNCTION,
        /** A method parameter. */
        PARAMETER,
        /** A constant. */
        CONSTANT
    }

    /**
     * Expected shape of the annotation arguments.
     */
    public enum Arguments {
        /** Marker annotation without arguments. */
        NONE,
        /** Zero or one bare name, e.g. {@code @@threadSafe(group)}. */
        OPTIONAL_NAME,
        /** Exactly one literal of any kind. */
        ONE_LITERAL,
        /** Exactly one number. */
        ONE_NUMBER,
        /** Exactly one non-negative integer. */
        ONE_NON_NEGATIVE_INTEGER,
        /** Exactly one string. */
        ONE_STRING,
        /** One or more bare names. */
        NAMES,
        /** Two or more bare names. */
        AT_LEAST_TWO_NAMES
    }

    private final String annotationName;
    private final Arguments arguments;
    private final Set<ElementKind> targets;

    KnownAnnotation(final String annotationName, final Arguments arguments, final ElementKind first,
                    final ElementKind... rest) {
        this.annotationName = annotationName;
        this.arguments = arguments;
        this.targets = Set.copyOf(EnumSet.of(first, rest));
    }

    /**
     * Returns the name as written after {@code @@}.
     *
     * @return the annotation name
     */
    public String annotationName() {
        return annotationName;
    }

    /**
     * Returns the expected argument shape.
     *
     * @return the argument shape
     */
    public Arguments arguments() {
        return arguments;
    }

    /**
     * Returns the element kinds this annotation may be attached to.
     *
     * @return the targets
     */
    public Set<ElementKind> targets() {
        return targets;
    }

    /**
     * Looks up an annotation by its name.
     *
     * @param name the name without {@code @@}
     * @return the annotation, if defined
     */
    public static Optional<KnownAnnotation> byName(final String name) {
        Objects.requireNonNull(name, "name must not be null");
        return Arrays.stream(values()).filter(a -> a.annotationName.equals(name)).findFirst();
    }
}
