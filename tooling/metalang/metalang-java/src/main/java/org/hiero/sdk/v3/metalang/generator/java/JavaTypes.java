package org.hiero.sdk.v3.metalang.generator.java;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Maps meta-language types to Java types (see "Type Mapping", "Numeric Types" and "Null handling" in
 * {@code guidelines/api-best-practices-java.md}).
 *
 * <ul>
 *   <li>{@code intX}/{@code uintX}: {@code byte} (X &le; 8), {@code short} (&le; 16), {@code int} (&le; 32),
 *       {@code long} (&le; 64), otherwise {@code BigInteger}; {@code uint8}/{@code uint16}/{@code uint32} use the
 *       next wider type ({@code short}/{@code int}/{@code long});
 *       {@code double}, {@code bool} likewise. The primitive
 *       is used unless the value is nullable or a type argument; then the wrapper class is used.</li>
 *   <li>Every module is {@code @NullMarked} (jspecify): unannotated types are non-null, exactly like the
 *       meta-language. Only {@code @@nullable} declarations get {@code @Nullable} (type-use syntax).</li>
 * </ul>
 */
final class JavaTypes {

    /** The jspecify package of {@code @NullMarked} / {@code @Nullable}. */
    static final String JSPECIFY = "org.jspecify.annotations";

    private JavaTypes() {
    }

    /**
     * Returns the Java type of a declaration (field, parameter, return value) including its nullness annotation.
     *
     * @param type     the meta-language type
     * @param nullable whether the declaration is {@code @@nullable}
     * @param imports  the imports of the file
     * @return the Java type, e.g. {@code int}, {@code @Nullable Integer}, {@code String}, {@code byte @Nullable []}
     */
    static String declaration(final Type type, final boolean nullable, final Imports imports) {
        return declaration(type, nullable, nullable, imports);
    }

    /**
     * Returns the Java type of a declaration including its nullness annotation, optionally with the wrapper class of
     * a primitive even if the declaration is not nullable (an accessor must keep the Java type of the accessor it
     * implements, e.g. {@code Long} for an inherited {@code $$T} or {@code @@nullable} attribute).
     *
     * @param type     the meta-language type
     * @param nullable whether the declaration is {@code @@nullable}
     * @param boxed    whether a primitive must be replaced by its wrapper class
     * @param imports  the imports of the file
     * @return the Java type
     */
    static String declaration(final Type type, final boolean nullable, final boolean boxed, final Imports imports) {
        final String java = type(type, nullable || boxed, imports);
        if (!nullable || isPrimitive(java) || java.equals("void")) {
            return java; // non-null is the default of the @NullMarked module
        }
        return annotate(java, imports.use(JSPECIFY, "Nullable"));
    }

    /**
     * Returns the Java type without nullness annotation.
     *
     * @param type    the meta-language type
     * @param boxed   whether a primitive must be replaced by its wrapper class
     * @param imports the imports of the file
     * @return the Java type
     * @throws UnsupportedTypeException if the type has no Java mapping yet
     */
    static String type(final Type type, final boolean boxed, final Imports imports) {
        Objects.requireNonNull(type, "type must not be null");
        return switch (type) {
            case Type.BasicType basic -> basic(basic, boxed, imports);
            case Type.DeclaredType declared -> imports.use(JavaNames.packageName(declared.name().namespace()),
                    declared.name().name()) + arguments(declared.arguments(), imports);
            case Type.TypeVariable variable -> imports.typeVariable(variable.name());
            case Type.WildcardType wildcard -> wildcard.upperBound() == null ? "?"
                    : "? extends " + type(wildcard.upperBound(), true, imports);
            case Type.AnyType ignored -> "Object";
            case Type.VoidType ignored -> boxed ? "Void" : "void";
            case Type.FunctionType function -> functional(function, imports);
            case Type.UnresolvedType unresolved -> throw new UnsupportedTypeException(unresolved.text());
        };
    }

    /**
     * Maps a function type by its shape (number of parameters; {@code void}, {@code bool} or another result) to a
     * {@code java.util.function} interface with wrapper types; the name of the function does not matter. Function
     * types with three or more parameters or varargs use a generated {@code @FunctionalInterface}
     * ({@link FunctionInterfaces}).
     */
    private static String functional(final Type.FunctionType function, final Imports imports) {
        if (!isStandard(function)) {
            final QualifiedName name = imports.functionInterfaces().of(function);
            return imports.use(JavaNames.packageName(name.namespace()), name.name());
        }
        final List<String> parameters = function.parameters().stream()
                .map(p -> argument(p.type(), p.hasAnnotation("nullable"), imports)).toList();
        final Type result = function.returnType();
        final boolean isVoid = result instanceof Type.VoidType;
        final boolean isBool = result instanceof Type.BasicType basic
                && basic.builtin().category() == BuiltinType.Category.BOOL;
        final String returned = isVoid ? "" : type(result, true, imports);
        return switch (parameters.size()) {
            case 0 -> isVoid ? "Runnable" : imports.use("java.util.function", "Supplier") + "<" + returned + ">";
            case 1 -> isVoid ? imports.use("java.util.function", "Consumer") + "<" + parameters.getFirst() + ">"
                    : isBool ? imports.use("java.util.function", "Predicate") + "<" + parameters.getFirst() + ">"
                    : imports.use("java.util.function", "Function") + "<" + parameters.getFirst() + ", "
                    + returned + ">";
            default -> {
                final String both = parameters.get(0) + ", " + parameters.get(1);
                yield isVoid ? imports.use("java.util.function", "BiConsumer") + "<" + both + ">"
                        : isBool ? imports.use("java.util.function", "BiPredicate") + "<" + both + ">"
                        : imports.use("java.util.function", "BiFunction") + "<" + both + ", " + returned + ">";
            }
        };
    }

    /**
     * Whether a {@code java.util.function} interface (or {@code Runnable}) matches the function type: at most two
     * parameters and no varargs.
     *
     * @param function the function type
     * @return {@code true} if no custom functional interface is needed
     */
    static boolean isStandard(final Type.FunctionType function) {
        return function.parameters().size() <= 2 && function.parameters().stream().noneMatch(p -> p.varargs());
    }

    private static String argument(final Type type, final boolean nullable, final Imports imports) {
        final String java = type(type, true, imports);
        return nullable ? annotate(java, imports.use(JSPECIFY, "Nullable")) : java;
    }

    private static String arguments(final List<Type> arguments, final Imports imports) {
        return arguments.isEmpty() ? ""
                : "<" + String.join(", ", arguments.stream().map(a -> type(a, true, imports)).toList()) + ">";
    }

    private static String basic(final Type.BasicType basic, final boolean boxed, final Imports imports) {
        final BuiltinType builtin = basic.builtin();
        return switch (builtin.category()) {
            case INTEGER -> integer(javaBits(builtin), boxed, imports);
            case FLOAT -> boxed ? "Double" : "double";
            case DECIMAL -> imports.use("java.math", "BigDecimal");
            case BOOL -> boxed ? "Boolean" : "boolean";
            case STRING -> "String";
            case BYTES -> "byte[]";
            case COLLECTION -> imports.use("java.util", builtin.name().equals("set") ? "Set" : "List")
                    + arguments(basic.arguments(), imports);
            case MAP -> imports.use("java.util", "Map") + arguments(basic.arguments(), imports);
            case TYPE -> "Class<" + (basic.arguments().isEmpty() ? "?"
                    : "? extends " + type(basic.arguments().getFirst(), true, imports)) + ">";
            case UUID -> imports.use("java.util", "UUID");
            case TEMPORAL -> imports.use("java.time", switch (builtin.name()) {
                case "date" -> "LocalDate";
                case "time" -> "LocalTime";
                case "dateTime" -> "LocalDateTime";
                default -> "ZonedDateTime";
            });
            case DURATION -> imports.use("java.time", "Duration");
            // per-item result of a stream (guidelines/java-files/StreamItem.java)
            case STREAM_RESULT -> imports.use(SupportFiles.STREAMING_PACKAGE, "StreamItem")
                    + arguments(basic.arguments(), imports);
        };
    }

    /**
     * Returns the width of the Java integer type of an {@code intX}/{@code uintX}. Java integers are signed, so
     * {@code uint8}, {@code uint16} and {@code uint32} use the next wider type ({@code short}, {@code int},
     * {@code long}) to hold all their values. {@code uint64} stays {@code long} (the usual Java convention for
     * unsigned 64-bit values, see {@code Long.toUnsignedString}).
     *
     * @param builtin the integer type
     * @return the width of the Java type
     */
    static int javaBits(final BuiltinType builtin) {
        return builtin.name().startsWith("uint") && builtin.bits() <= 32 ? builtin.bits() * 2 : builtin.bits();
    }

    private static String integer(final int bits, final boolean boxed, final Imports imports) {
        if (bits <= 8) {
            return boxed ? "Byte" : "byte";
        } else if (bits <= 16) {
            return boxed ? "Short" : "short";
        } else if (bits <= 32) {
            return boxed ? "Integer" : "int";
        } else if (bits <= 64) {
            return boxed ? "Long" : "long";
        }
        return imports.use("java.math", "BigInteger");
    }

    /**
     * Whether the Java type is a primitive type.
     *
     * @param java the Java type
     * @return {@code true} for primitives
     */
    static boolean isPrimitive(final String java) {
        return switch (java) {
            case "byte", "short", "int", "long", "double", "boolean" -> true;
            default -> false;
        };
    }

    /**
     * Puts a type-use annotation at the right place: in front of the simple type ({@code @Nullable String}) or in
     * front of the array brackets ({@code byte @Nullable []}).
     */
    static String annotate(final String java, final String annotation) {
        if (java.endsWith("[]")) {
            return java.substring(0, java.length() - 2) + " @" + annotation + " []";
        }
        final int lastDot = java.contains("<") ? java.substring(0, java.indexOf('<')).lastIndexOf('.')
                : java.lastIndexOf('.');
        if (lastDot >= 0) {
            return java.substring(0, lastDot + 1) + "@" + annotation + " " + java.substring(lastDot + 1);
        }
        return "@" + annotation + " " + java;
    }

    /**
     * Thrown for meta-language types that have no Java mapping yet.
     */
    static final class UnsupportedTypeException extends RuntimeException {

        UnsupportedTypeException(final String type) {
            super("Type '" + type + "' has no Java mapping yet");
        }

        private UnsupportedTypeException(final String message, final boolean ignored) {
            super(message);
        }

        /**
         * Creates an exception with the given message.
         *
         * @param message the message
         * @return the exception
         */
        static UnsupportedTypeException withMessage(final String message) {
            return new UnsupportedTypeException(message, true);
        }
    }
}
